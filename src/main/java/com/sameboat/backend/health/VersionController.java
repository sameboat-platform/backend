package com.sameboat.backend.health;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class VersionController {
    private final String version;

    public VersionController(Environment env) {
        // Try to get Implementation-Version from MANIFEST.MF
        String version = null;
        try {
            version = getClass().getPackage().getImplementationVersion();
        } catch (Exception ignored) {}
        if (version == null || version.isBlank()) {
            version = env.getProperty("project.version", "unknown");
        }
        this.version = version;
    }

    @GetMapping("/api/version")
    public Map<String, String> getVersion() {
        return Map.of("version", version);
    }
}
