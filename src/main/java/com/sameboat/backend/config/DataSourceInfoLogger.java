package com.sameboat.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Simple startup logger that outputs a sanitized datasource URL (credentials masked).
 * Implements ApplicationListener to hook into the ApplicationReadyEvent, which waits until
 * the application is fully started before logging.
 * @author ArchILLtect
 */
@Component
public class DataSourceInfoLogger implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger log = LoggerFactory.getLogger(DataSourceInfoLogger.class);

    // Pulls datasource URL and username from application properties or "" if not set
    @Value("${spring.datasource.url:}")
    private String rawUrl;

    // Pulls datasource username from application properties or "" if not set
    @Value("${spring.datasource.username:}")
    private String username;

    // Injected Spring Environment for accessing active profiles
    private final Environment env;

    /**
     * Constructor with injected Environment.
     * @param env the Spring Environment for accessing active profiles
     */
    public DataSourceInfoLogger(Environment env) { this.env = env; }

    /**
     * Logs the sanitized datasource URL and username when the application is ready.
     * Logs everything nicely after startup to avoid cluttering the startup logs.
     * @param event the ApplicationReadyEvent
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        String sanitized = sanitize(rawUrl);
        if (sanitized == null || sanitized.isBlank()) {
            log.warn("No spring.datasource.url resolved");
        } else {
            log.info("Datasource URL: {}", sanitized);
        }
        if (username != null && !username.isBlank()) {
            log.info("Datasource user: {}", username);
        }
        log.debug("Active profiles: {}", String.join(",", env.getActiveProfiles()));
    }

    /**
     * Sanitizes the datasource URL by masking credentials if present.
     * Prevents logging sensitive information by replacing username:password@ with ***@.
     * @param url the raw datasource URL
     * @return the sanitized URL with credentials masked
     */
    private String sanitize(String url) {
        if (url == null) return null;
        int schemeIdx = url.indexOf("//");
        int atIdx = url.indexOf('@');
        if (schemeIdx > 0 && atIdx > schemeIdx) {
            String creds = url.substring(schemeIdx + 2, atIdx);
            if (creds.contains(":")) {
                return url.substring(0, schemeIdx + 2) + "***@" + url.substring(atIdx + 1);
            }
        }
        return url;
    }
}
