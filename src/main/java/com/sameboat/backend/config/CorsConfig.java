package com.sameboat.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Registers a permissive CORS configuration constrained to the allowed origins
 * declared in {@link SameboatProperties.Cors}. Credentials are enabled so the
 * session cookie can be transmitted by browsers.
 * @see SameboatProperties
 * @author ArchILLtect
 *
 */
@Configuration
public class CorsConfig {

    private final SameboatProperties props;
    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    // Constructor with injected SameboatProperties
    public CorsConfig(SameboatProperties props) { this.props = props; }

    /**
     * Defines the CORS configuration source bean.
     * @return the CorsConfigurationSource with allowed origins and settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        for (String rawOrigin : props.getCors().getAllowedOrigins()) {
            String origin = rawOrigin.trim();
            if (origin.isEmpty()) continue;
            if (origin.contains("*")) {
                cfg.addAllowedOriginPattern(origin);
            } else {
                cfg.addAllowedOrigin(origin);
            }
        }
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true); // allow cookie / credentialed requests
        cfg.setExposedHeaders(List.of("Set-Cookie"));
        cfg.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        log.info("CORS allowed origins: {}", cfg.getAllowedOrigins());
        log.info("CORS allowed origin patterns: {}", cfg.getAllowedOriginPatterns());
        return source;
    }
}