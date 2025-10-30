package com.sameboat.backend.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "sameboat.endpoints.user-read=true")
@Disabled("User read endpoint is property-gated and not part of MVP; re-enable when exposing admin/public profile")
class UserControllerGetByIdIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /users/{id} -> 404 NOT_FOUND when user missing (authenticated)")
    void getUserById_notFound() throws Exception {
        // Authenticate first (register to get a session cookie)
        var reg = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notfound_auth@example.com\",\"password\":\"Aa12345!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String cookie = reg.getResponse().getHeader("Set-Cookie").split(";",2)[0];

        mvc.perform(get("/users/{id}", UUID.randomUUID()).header("Cookie", cookie).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 returns public user DTO when self (authenticated)")
    void getUserById_ok_self() throws Exception {
        String email = "getbyid@example.com";
        var reg = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Aa12345!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn();
        String cookie = reg.getResponse().getHeader("Set-Cookie").split(";",2)[0];
        String body = reg.getResponse().getContentAsString();
        String userId = body.substring(body.indexOf(":\"") + 2).replace("\"}", "");

        // Self should be allowed and return public DTO (no email), assert displayName
        mvc.perform(get("/users/{id}", UUID.fromString(userId)).header("Cookie", cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").exists());
    }
}
