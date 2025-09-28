package com.sameboat.backend.auth;

import com.sameboat.backend.auth.dto.LoginRequest;
import com.sameboat.backend.auth.dto.LoginResponse;
import com.sameboat.backend.auth.session.SessionService;
import com.sameboat.backend.common.ErrorResponse;
import com.sameboat.backend.user.UserMapper;
import com.sameboat.backend.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final SessionService sessionService;

    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        if (!"dev".equals(request.password())) {
            log.info("Login failed email={}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("UNAUTHORIZED", "Invalid credentials"));
        }
        var user = userService.findOrCreateByEmail(request.email());
        var session = sessionService.createSession(user.getId(), Duration.ofDays(7));
        log.info("Login success userId={} email={} sessionId={}", user.getId(), user.getEmail(), session.getId());

        Cookie cookie = new Cookie("SBSESSION", session.getId().toString()); // use UUID id as opaque token
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(false); // can be toggled for prod
        cookie.setMaxAge((int) Duration.ofDays(7).toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponse(UserMapper.toDto(user)));
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
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
}
