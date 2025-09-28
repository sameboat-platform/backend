package com.sameboat.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Validated
@ConfigurationProperties(prefix = "sameboat")
@Getter
public class SameboatProperties {
    private final Auth auth = new Auth();
    private final Cookie cookie = new Cookie();
    private final Session session = new Session();
    private final Cors cors = new Cors();

    @Getter @Setter
    public static class Auth {
        /** Auto-create user on login in dev/test when password matches stub. */
        private boolean devAutoCreate = false;
        private String stubPassword = "dev";
    }
    @Getter @Setter
    public static class Cookie {
        private boolean secure = false;
        private String domain = ""; // empty -> no domain attribute
    }
    @Getter @Setter
    public static class Session {
        private int ttlDays = 7;
    }
    @Getter @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
