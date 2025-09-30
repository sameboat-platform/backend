package com.sameboat.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the SameBoat backend Spring Boot application.
 * <p>
 * Enables component scanning, auto-configuration, and property binding via
 * {@link ConfigurationPropertiesScan}. Starts the embedded web server.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SameboatBackendApplication {

    /**
     * Bootstraps the application.
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        SpringApplication.run(SameboatBackendApplication.class, args);
    }
}
