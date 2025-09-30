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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpiredSessionIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired SessionRepository sessionRepository;
    @Autowired PlatformTransactionManager txManager;
    @Autowired EntityManager entityManager;

    private String obtainSessionCookie() throws Exception {
        final String email = "expire@test.com";
        var res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"dev\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("SBSESSION=");
        int semi = setCookie.indexOf(';');
        return semi >= 0 ? setCookie.substring(0, semi) : setCookie;
    }

    private void expireSessionNow(UUID sessionId, long minutesAgo) {
        new TransactionTemplate(txManager).executeWithoutResult(status -> {
            SessionEntity s = sessionRepository.findById(sessionId).orElseThrow();
            var farPastUtc = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).minusDays(30);
            s.setExpiresAt(farPastUtc);
            sessionRepository.save(s);
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Test
    @DisplayName("Expired session returns SESSION_EXPIRED envelope")
    void expiredSessionUnauthorized() throws Exception {
        String cookieHeader = obtainSessionCookie();
        String token = cookieHeader.substring(cookieHeader.indexOf('=') + 1);
        UUID sessionId = UUID.fromString(token);
        expireSessionNow(sessionId, 5);
        var s = sessionRepository.findById(sessionId).orElseThrow();
        System.out.println("TEST DIAG expiredSessionUnauthorized expiresAt=" + s.getExpiresAt() + " now=" + Instant.now());
        assertThat(s.getExpiresAt().toInstant()).isBefore(Instant.now());
        mvc.perform(get("/me").cookie(new Cookie("SBSESSION", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESSION_EXPIRED"));
    }

    @Test
    @DisplayName("Expired session via sb_session cookie returns SESSION_EXPIRED")
    void expiredSessionAliasCookie() throws Exception {
        String cookieHeader = obtainSessionCookie();
        String token = cookieHeader.substring(cookieHeader.indexOf('=') + 1);
        UUID sessionId = UUID.fromString(token);
        expireSessionNow(sessionId, 10);
        var s = sessionRepository.findById(sessionId).orElseThrow();
        System.out.println("TEST DIAG expiredSessionAliasCookie expiresAt=" + s.getExpiresAt() + " now=" + Instant.now());
        assertThat(s.getExpiresAt().toInstant()).isBefore(Instant.now());
        mvc.perform(get("/me").cookie(new Cookie("sb_session", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESSION_EXPIRED"));
    }
}
