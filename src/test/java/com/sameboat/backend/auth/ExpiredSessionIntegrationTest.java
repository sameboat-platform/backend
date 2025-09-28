package com.sameboat.backend.auth;

import com.sameboat.backend.auth.session.SessionEntity;
import com.sameboat.backend.auth.session.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpiredSessionIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    SessionRepository sessionRepository;

    private String obtainSessionCookie() throws Exception {
        final String email = "expire@test.com"; // single test account
        var res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"dev\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).as("Expect Set-Cookie header with SBSESSION").isNotNull();
        assertThat(setCookie).contains("SBSESSION=");
        int semi = setCookie.indexOf(';');
        return semi >= 0 ? setCookie.substring(0, semi) : setCookie;
    }

    @Test
    @DisplayName("Expired session returns SESSION_EXPIRED envelope")
    void expiredSessionUnauthorized() throws Exception {
        String cookieHeader = obtainSessionCookie();
        String token = cookieHeader.substring(cookieHeader.indexOf('=') + 1);
        UUID sessionId = UUID.fromString(token);
        SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
        session.setExpiresAt(OffsetDateTime.now().minusMinutes(5));
        sessionRepository.save(session);

        mvc.perform(get("/me").header("Cookie", cookieHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESSION_EXPIRED"));
    }
}
