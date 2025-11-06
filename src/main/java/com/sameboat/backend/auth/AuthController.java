package com.sameboat.backend.auth;

import com.sameboat.backend.auth.dto.LoginResponse;
import com.sameboat.backend.auth.dto.RegisterRequest;
import com.sameboat.backend.auth.session.SessionService;
import com.sameboat.backend.common.ErrorResponse;
import com.sameboat.backend.config.SameboatProperties;
import com.sameboat.backend.user.UserMapper;
import com.sameboat.backend.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * REST controller providing authentication endpoints: register, login and logout.
 * On successful login/registration a session cookie (SBSESSION) is issued. Logout
 * invalidates the persisted session and expires the cookie client-side.
 */
@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final SameboatProperties props;
    private final RateLimiterService rateLimiter;

    /**
     * Constructor with dependencies injected.
     * @param userService             the service managing user accounts
     * @param sessionService        the service managing user sessions
     * @param passwordEncoder   the password encoder for hashing and verifying passwords
     * @param props                      application configuration properties
     * @param rateLimiter              the rate limiter service for login attempts
     */
    public AuthController(UserService userService, SessionService sessionService, PasswordEncoder passwordEncoder, SameboatProperties props, RateLimiterService rateLimiter) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
        this.rateLimiter = rateLimiter;
    }

    private String resolveCookieDomain(jakarta.servlet.http.HttpServletRequest request) {
        String explicit = props.getCookie().getDomain();
        if (explicit != null && !explicit.isBlank()) {
            // Defensive: strip leading dot if present
            return explicit.startsWith(".") ? explicit.substring(1) : explicit;
        }
        String forwarded = request != null ? request.getHeader("X-Forwarded-Host") : null;
        String host = (forwarded != null && !forwarded.isBlank()) ? forwarded : (request != null ? request.getServerName() : null);
        if (host == null || host.isBlank()) return null;
        host = host.toLowerCase();
        for (String apex : props.getCookie().getValidDomains()) {
            if (host.equals(apex) || host.endsWith("." + apex)) {
                return apex; // return apex domain without leading dot
            }
        }
        return null; // default to host-only cookie
    }

    /**
     * Builds the session cookie with appropriate attributes.
     * @param token the session token value
     * @return  the constructed Cookie object
     */
    private Cookie buildSessionCookie(String token, jakarta.servlet.http.HttpServletRequest request) {
        var sessionCfg = props.getSession();
        var cookieCfg = props.getCookie();
        Cookie cookie = new Cookie("SBSESSION", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(cookieCfg.isSecure());
        cookie.setMaxAge((int) java.time.Duration.ofDays(sessionCfg.getTtlDays()).toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        String domain = resolveCookieDomain(request);
        if (domain != null && !domain.isBlank()) cookie.setDomain(domain);
        return cookie;
    }

    /**
     * Convenience builder for a uniform bad credentials response.
     * @param email the email used in the failed login attempt
     * @return  the ResponseEntity with error details
     */
    private ResponseEntity<ErrorResponse> badCredentials(String email) {
        log.info("Login failed email={} (bad credentials)", email);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("BAD_CREDENTIALS", "Email or password is incorrect"));
    }

    /**
     * Convenience builder for a rate limited response.
     * @param key the rate limit key that was exceeded
     * @return  the ResponseEntity with error details
     */
    private ResponseEntity<ErrorResponse> rateLimited(String key) {
        log.info("Rate limited response for key={}", key);
        return ResponseEntity.status(429)
                .body(new ErrorResponse("RATE_LIMITED", "Too many attempts; try again later"));
    }

    /**
     * Builds a rate limiting key based on normalized email and request IP.
     * @param emailNorm the normalized email address
     * @param request   the HTTP servlet request
     * @return  the constructed rate limit key
     */
    private String rateKey(String emailNorm, jakarta.servlet.http.HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : "";
        return emailNorm + "|" + ip;
    }

    /**
     * Registers a new user account (unless email already exists) and immediately
     * creates a session, returning the new user id plus issuing the cookie.
     * @param request   the registration request payload
     * @param response  the HTTP servlet response to add the session cookie to
     * @return          the ResponseEntity with new user id or error details
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response,
                                      jakarta.servlet.http.HttpServletRequest httpRequest) {
        String emailNorm = userService.normalizeEmail(request.email());
        if (userService.getByEmailNormalized(emailNorm).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("EMAIL_EXISTS", "Email already registered"));
        }
        var user = userService.registerNew(emailNorm, request.password(), passwordEncoder);
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }
        var session = sessionService.createSession(user.getId(), java.time.Duration.ofDays(props.getSession().getTtlDays()));
        response.addCookie(buildSessionCookie(session.getId().toString(), httpRequest));
        log.info("Registration success userId={} email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("userId", user.getId().toString()));
    }

    /**
     * Authenticates a user by email + password, optionally auto-creating a dev
     * user if configured. Issues a fresh session cookie on success.
     * @param request       the login request payload
     * @param httpRequest   the HTTP servlet request for rate limiting info
     * @param response      the HTTP servlet response to add the session cookie to
     * @return              the ResponseEntity with user details or error info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid com.sameboat.backend.auth.dto.LoginRequest request,
                                   jakarta.servlet.http.HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        return loginInternal(request.email(), request.password(), httpRequest, response);
    }

    /**
     * Accepts classic form posts as well (application/x-www-form-urlencoded).
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginForm(@RequestParam("email") String email,
                                       @RequestParam("password") String password,
                                       jakarta.servlet.http.HttpServletRequest httpRequest,
                                       HttpServletResponse response) {
        return loginInternal(email, password, httpRequest, response);
    }

    private ResponseEntity<?> loginInternal(String emailRaw,
                                            String passwordRaw,
                                            jakarta.servlet.http.HttpServletRequest httpRequest,
                                            HttpServletResponse response) {
        String emailNorm = userService.normalizeEmail(emailRaw);
        String key = rateKey(emailNorm, httpRequest);
        if (rateLimiter.isLimited(key)) {
            return rateLimited(key);
        }
        var userOpt = userService.getByEmailNormalized(emailNorm);
        var authCfg = props.getAuth();
        if (userOpt.isEmpty()) {
            log.debug("Login failed - user not found email={}", emailNorm);
            if (authCfg.isDevAutoCreate() && authCfg.getStubPassword().equals(passwordRaw)) {
                log.info("Auto-creating dev user email={}", emailNorm);
                var created = userService.registerNew(emailNorm, passwordRaw, passwordEncoder);
                var session = sessionService.createSession(created.getId(), java.time.Duration.ofDays(props.getSession().getTtlDays()));
                response.addCookie(buildSessionCookie(session.getId().toString(), httpRequest));
                rateLimiter.reset(key);
                return ResponseEntity.ok(new LoginResponse(UserMapper.toDto(created)));
            }
            if (rateLimiter.recordFailure(key)) {
                return rateLimited(key);
            }
            return badCredentials(emailNorm);
        }
        var user = userOpt.get();
        if (!userService.passwordMatches(user, passwordRaw, passwordEncoder)) {
            log.debug("Login failed - password mismatch email={}", emailNorm);
            if (rateLimiter.recordFailure(key)) {
                return rateLimited(key);
            }
            return badCredentials(emailNorm);
        }
        var session = sessionService.createSession(user.getId(), java.time.Duration.ofDays(props.getSession().getTtlDays()));
        log.info("Login success userId={} email={} sessionId={}", user.getId(), user.getEmail(), session.getId());
        response.addCookie(buildSessionCookie(session.getId().toString(), httpRequest));
        rateLimiter.reset(key);
        return ResponseEntity.ok(new LoginResponse(UserMapper.toDto(user)));
    }

    /**
     * Logs out the current session (if present) and expires the cookie.
     * @param token     the session token from the SBSESSION cookie
     * @param response  the HTTP servlet response to add the expired cookie to
     * @return          the ResponseEntity indicating no content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "SBSESSION", required = false) String token, HttpServletResponse response,
                                       jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (token != null) {
            sessionService.invalidate(token);
            log.info("Logout token={}", token);
        }
        var cookieCfg = props.getCookie();
        Cookie cookie = new Cookie("SBSESSION", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieCfg.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        String domain = resolveCookieDomain(httpRequest);
        if (domain != null && !domain.isBlank()) cookie.setDomain(domain);
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
}