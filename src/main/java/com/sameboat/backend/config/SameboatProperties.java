package com.sameboat.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Strongly typed configuration properties for the SameBoat application grouped under the
 * prefix {@code sameboat}. Provides structured access to authentication, session, CORS and
 * cookie settings.
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.bindings">Spring Boot Configuration Properties</a>
 * @author ArchILLtect
 */
@Validated
@ConfigurationProperties(prefix = "sameboat")
@Getter
public class SameboatProperties {
    private final Auth auth = new Auth();
    private final Cookie cookie = new Cookie();
    private final Session session = new Session();
    private final Cors cors = new Cors();

    /** Authentication related toggles (dev conveniences). */
    @Getter @Setter
    public static class Auth {
        /** Auto-create user on login in dev/test when password matches stub. */
        private boolean devAutoCreate = false;
        /** Password that allows dev auto creation when enabled. */
        private String stubPassword = "dev";
    }
    /** Cookie attribute customization. */
    @Getter @Setter
    public static class Cookie {
        /** Whether the session cookie should be marked Secure (HTTPS only). */
        private boolean secure = false;
        /** Optional explicit cookie domain (blank => omit attribute). */
        private String domain = ""; // empty -> no domain attribute
    }
    /** Session lifetime configuration. */
    @Getter @Setter
    public static class Session {
        /** Session time-to-live in days. */
        private int ttlDays = 7;
    }
    /** Cross-Origin Resource Sharing settings. */
    @Getter @Setter
    public static class Cors {
        /** Whitelisted allowed origins for browser requests that include credentials. */
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
