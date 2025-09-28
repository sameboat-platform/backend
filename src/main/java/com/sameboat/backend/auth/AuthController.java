package com.sameboat.backend.auth;

import com.sameboat.backend.auth.dto.LoginRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final SameboatProperties props;

    public AuthController(UserService userService, SessionService sessionService, PasswordEncoder passwordEncoder, SameboatProperties props) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    private Cookie buildSessionCookie(String token) {
        var sessionCfg = props.getSession();
        var cookieCfg = props.getCookie();
        Cookie cookie = new Cookie("SBSESSION", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(cookieCfg.isSecure());
        cookie.setMaxAge((int) java.time.Duration.ofDays(sessionCfg.getTtlDays()).toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        if (cookieCfg.getDomain() != null && !cookieCfg.getDomain().isBlank()) cookie.setDomain(cookieCfg.getDomain());
        return cookie;
    }

    private ResponseEntity<ErrorResponse> badCredentials(String email) {
        log.info("Login failed email={} (bad credentials)", email);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("BAD_CREDENTIALS", "Email or password is incorrect"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response) {
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
        response.addCookie(buildSessionCookie(session.getId().toString()));
        log.info("Registration success userId={} email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("userId", user.getId().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid com.sameboat.backend.auth.dto.LoginRequest request, HttpServletResponse response) {
        String emailNorm = userService.normalizeEmail(request.email());
        var userOpt = userService.getByEmailNormalized(emailNorm);
        var authCfg = props.getAuth();
        if (userOpt.isEmpty()) {
            if (authCfg.isDevAutoCreate() && authCfg.getStubPassword().equals(request.password())) {
                log.info("Auto-creating dev user email={}", emailNorm);
                var created = userService.registerNew(emailNorm, request.password(), passwordEncoder);
                var session = sessionService.createSession(created.getId(), java.time.Duration.ofDays(props.getSession().getTtlDays()));
                response.addCookie(buildSessionCookie(session.getId().toString()));
                return ResponseEntity.ok(new LoginResponse(com.sameboat.backend.user.UserMapper.toDto(created)));
            }
            return badCredentials(emailNorm);
        }
        var user = userOpt.get();
        if (!userService.passwordMatches(user, request.password(), passwordEncoder)) {
            return badCredentials(emailNorm);
        }
        var session = sessionService.createSession(user.getId(), java.time.Duration.ofDays(props.getSession().getTtlDays()));
        log.info("Login success userId={} email={} sessionId={}", user.getId(), user.getEmail(), session.getId());
        response.addCookie(buildSessionCookie(session.getId().toString()));
        return ResponseEntity.ok(new LoginResponse(com.sameboat.backend.user.UserMapper.toDto(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "SBSESSION", required = false) String token, HttpServletResponse response) {
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
        if (cookieCfg.getDomain() != null && !cookieCfg.getDomain().isBlank()) cookie.setDomain(cookieCfg.getDomain());
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
}
