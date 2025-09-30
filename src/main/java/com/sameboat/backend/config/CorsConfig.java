package com.sameboat.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * Registers a permissive CORS configuration constrained to the allowed origins
 * declared in {@link SameboatProperties.Cors}. Credentials are enabled so the
 * session cookie can be transmitted by browsers.
 */
@Configuration
public class CorsConfig {

    private final SameboatProperties props;

    public CorsConfig(SameboatProperties props) { this.props = props; }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        for (String origin : props.getCors().getAllowedOrigins()) {
            String o = origin.trim();
            if (!o.isEmpty()) cfg.addAllowedOrigin(o);
        }
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true); // allow cookie / credentialed requests
        cfg.setExposedHeaders(List.of("Set-Cookie"));
        cfg.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}