package com.sameboat.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that CORS configuration allows the configured origin and does not reflect an unapproved origin.
 * Uses the test profile but overrides the allowed origins with a production-like domain.
 */
@SpringBootTest(properties = {
        "sameboat.cors.allowed-origins=https://app.sameboatplatform.org"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Preflight request from allowed origin gets CORS allow headers")
    void preflightAllowedOrigin() throws Exception {
        mvc.perform(options("/health")
                        .header("Origin", "https://app.sameboatplatform.org")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.sameboatplatform.org"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Preflight from disallowed origin gets 403 and no allow origin header")
    void preflightDisallowedOrigin() throws Exception {
        mvc.perform(options("/health")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
