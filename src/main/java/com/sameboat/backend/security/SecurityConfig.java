package com.sameboat.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sameboat.backend.common.ErrorResponse;
import com.sameboat.backend.auth.session.SessionService;
import com.sameboat.backend.user.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    private static final class JsonAuthEntryPoint implements AuthenticationEntryPoint {
        private static final Logger log = LoggerFactory.getLogger(JsonAuthEntryPoint.class);
        private final ObjectMapper objectMapper;
        private JsonAuthEntryPoint(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
        @Override
        public void commence(@NonNull jakarta.servlet.http.HttpServletRequest request,
                              @NonNull jakarta.servlet.http.HttpServletResponse response,
                              AuthenticationException authException) throws java.io.IOException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            boolean expired = Boolean.TRUE.equals(request.getAttribute("sameboat.sessionExpired"));
            if (expired) {
                log.debug("AuthEntryPoint: using SESSION_EXPIRED (attribute present)");
            }
            String code = expired ? "SESSION_EXPIRED" : "UNAUTHENTICATED";
            String message = expired ? "Session expired" : "Authentication required";
            ErrorResponse body = new ErrorResponse(code, message);
            try (var out = response.getOutputStream()) {
                objectMapper.writeValue(out, body);
            }
        }
    }

    @Bean
    public AuthenticationEntryPoint jsonAuthEntryPoint(ObjectMapper objectMapper) {
        return new JsonAuthEntryPoint(objectMapper);
    }

    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter(SessionService sessionService, UserService userService) {
        return new SessionAuthenticationFilter(sessionService, userService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http,
                                                   @NonNull SessionAuthenticationFilter sessionAuthenticationFilter,
                                                   @NonNull AuthenticationEntryPoint jsonAuthEntryPoint) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/actuator/health").permitAll()
                        .requestMatchers("/health", "/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/version").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jsonAuthEntryPoint)
                        .accessDeniedHandler(new AccessDeniedHandlerImpl()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
