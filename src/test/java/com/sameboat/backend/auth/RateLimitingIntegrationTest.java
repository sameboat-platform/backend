package com.sameboat.backend.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitingIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Login rate limit triggers 429 after 5 failed attempts")
    void loginRateLimited() throws Exception {
        // The test user 'ratelimit@example.com' is auto-created by the test profile with password 'dev'.
        // We use an incorrect password ('NotTheStub123') to trigger failed login attempts.
        String email = "ratelimit@example.com";
        String badJson = "{\"email\":\"ratelimit@example.com\",\"password\":\"NotTheStub123\"}"; // intentionally incorrect
        // First 4 should be BAD_CREDENTIALS (401)
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("BAD_CREDENTIALS"));
        }
        // 5th hits the limiter -> 429
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("RATE_LIMITED"));
        // 6th still limited (short window)
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("RATE_LIMITED"));
    }
}
