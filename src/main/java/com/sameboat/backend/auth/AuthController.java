package com.sameboat.backend.auth;

import com.sameboat.backend.auth.dto.LoginRequest;
import com.sameboat.backend.auth.dto.LoginResponse;
import com.sameboat.backend.auth.dto.RegisterRequest;
import com.sameboat.backend.auth.session.SessionService;
import com.sameboat.backend.common.ErrorResponse;
import com.sameboat.backend.user.UserMapper;
import com.sameboat.backend.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${sameboat.auth.dev-auto-create:false}")
    private boolean devAutoCreate;
    @Value("${sameboat.auth.stub-password:dev}")
    private String stubPassword;
    @Value("${sameboat.cookie.secure:false}")
    private boolean cookieSecure;
    @Value("${sameboat.cookie.domain:}")
    private String cookieDomain;
    @Value("${sameboat.session.ttl-days:7}")
    private int sessionTtlDays;

    public AuthController(UserService userService, SessionService sessionService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
    }

    private Cookie buildSessionCookie(String token) {
        Cookie cookie = new Cookie("SBSESSION", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge((int) java.time.Duration.ofDays(sessionTtlDays).toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        if (cookieDomain != null && !cookieDomain.isBlank()) cookie.setDomain(cookieDomain);
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
        var session = sessionService.createSession(user.getId(), java.time.Duration.ofDays(sessionTtlDays));
        response.addCookie(buildSessionCookie(session.getId().toString()));
        log.info("Registration success userId={} email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("userId", user.getId().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid com.sameboat.backend.auth.dto.LoginRequest request, HttpServletResponse response) {
        String emailNorm = userService.normalizeEmail(request.email());
        var userOpt = userService.getByEmailNormalized(emailNorm);
        if (userOpt.isEmpty()) {
            if (devAutoCreate && stubPassword.equals(request.password())) {
                log.info("Auto-creating dev user email={}", emailNorm);
                var created = userService.registerNew(emailNorm, request.password(), passwordEncoder);
                var session = sessionService.createSession(created.getId(), java.time.Duration.ofDays(sessionTtlDays));
                response.addCookie(buildSessionCookie(session.getId().toString()));
                return ResponseEntity.ok(new LoginResponse(com.sameboat.backend.user.UserMapper.toDto(created)));
            }
            return badCredentials(emailNorm);
        }
        var user = userOpt.get();
        if (!userService.passwordMatches(user, request.password(), passwordEncoder)) {
            return badCredentials(emailNorm);
        }
        var session = sessionService.createSession(user.getId(), java.time.Duration.ofDays(sessionTtlDays));
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
        Cookie cookie = new Cookie("SBSESSION", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", "Lax");
        if (cookieDomain != null && !cookieDomain.isBlank()) cookie.setDomain(cookieDomain);
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
}
