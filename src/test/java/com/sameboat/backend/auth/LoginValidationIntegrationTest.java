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
class LoginValidationIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("POST /auth/login with wrong content-type returns 415 VALIDATION_ERROR")
    void loginWrongContentType() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=a@b.com&password=x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/login with empty body returns 400 VALIDATION_ERROR")
    void loginEmptyBody() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
