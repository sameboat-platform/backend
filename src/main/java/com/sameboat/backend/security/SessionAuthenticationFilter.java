package com.sameboat.backend.security;

import com.sameboat.backend.auth.AuthPrincipal;
import com.sameboat.backend.auth.session.SessionService;
import com.sameboat.backend.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationFilter.class);

    private final SessionService sessionService;
    private final UserService userService;

    public SessionAuthenticationFilter(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = extractSessionCookie(request);
            if (token != null) {
                log.debug("Session cookie detected token={}", token);
                try {
                    var uuid = UUID.fromString(token);
                    var sessionOpt = sessionService.findById(uuid);
                    if (sessionOpt.isPresent()) {
                        var session = sessionOpt.get();
                        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(java.time.OffsetDateTime.now())) {
                            log.warn("Expired session token={} userId={}", token, session.getUserId());
                            request.setAttribute("sameboat.sessionExpired", true);
                        } else {
                            var userOpt = userService.findById(session.getUserId());
                            if (userOpt.isPresent()) {
                                var user = userOpt.get();
                                log.info("Authenticated user id={} email={}", user.getId(), user.getEmail());
                                sessionService.touch(session);
                                var principal = new AuthPrincipal(user.getId(), user.getEmail(), user.getRole());
                                var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            } else {
                                log.warn("User not found for session userId={} token={}", session.getUserId(), token);
                            }
                        }
                    } else {
                        log.warn("No session found for token={}", token);
                    }
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid session token format token={}", token);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("SBSESSION".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
