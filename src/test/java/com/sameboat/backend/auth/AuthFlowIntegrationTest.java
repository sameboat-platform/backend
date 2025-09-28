package com.sameboat.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Login with correct stub password returns cookie and user")
    void loginSuccess() throws Exception {
        var res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"dev\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();
        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("SBSESSION=");
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.path("user").path("email").asText()).isEqualTo("a@b.com");
    }

    @Test
    @DisplayName("Login failure returns BAD_CREDENTIALS error envelope")
    void loginFailure() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("BAD_CREDENTIALS"));
    }

    @Test
    @DisplayName("/me UNAUTHENTICATED without cookie")
    void meUnauthorized() throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("/me authorized after login and revoked after logout")
    void meAuthorizedThenLogout() throws Exception {
        var login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"u@e.com\",\"password\":\"dev\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String setCookie = login.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        Pattern p = Pattern.compile("SBSESSION=([^;]+)");
        Matcher m = p.matcher(setCookie);
        assertThat(m.find()).as("SBSESSION cookie present").isTrue();
        String token = m.group(1);
        Cookie sessionCookie = new Cookie("SBSESSION", token);

        mvc.perform(get("/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("u@e.com"));

        mvc.perform(post("/auth/logout").cookie(sessionCookie))
                .andExpect(status().isNoContent());

        mvc.perform(get("/me").cookie(sessionCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }
}
