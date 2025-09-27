package com.sameboat.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

@Component
public class DataSourceInfoLogger implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger log = LoggerFactory.getLogger(DataSourceInfoLogger.class);

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    private final Environment env;

    public DataSourceInfoLogger(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        String sanitized = sanitize(rawUrl);
        if (sanitized == null || sanitized.isBlank()) {
            log.warn("No spring.datasource.url resolved (check environment variables). Using defaults?");
        } else if (!sanitized.startsWith("jdbc:postgresql://")) {
            log.warn("Datasource URL does not start with jdbc:postgresql:// -> {}", sanitized);
        } else {
            log.info("Datasource URL: {}", sanitized);
        }
        if (username != null && !username.isBlank()) {
            log.info("Datasource user: {}", username);
        }
        log.debug("Active profiles: {}", String.join(",", env.getActiveProfiles()));
    }

    private String sanitize(String url) {
        if (url == null) return null;
        // Mask credentials if embedded like jdbc:postgresql://user:pass@host/...
        int schemeIdx = url.indexOf("//");
        int atIdx = url.indexOf('@');
        if (schemeIdx > 0 && atIdx > schemeIdx) {
            // find credentials segment
            String creds = url.substring(schemeIdx + 2, atIdx);
            if (creds.contains(":")) {
                return url.substring(0, schemeIdx + 2) + "***@" + url.substring(atIdx + 1);
            }
        }
        return url;
    }
}
