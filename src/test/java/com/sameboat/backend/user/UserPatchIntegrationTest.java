package com.sameboat.backend.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPatchIntegrationTest {

    @Autowired
    MockMvc mvc;

    private String loginAndGetCookie(String email) throws Exception {
        var res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"dev\"}"))
                .andReturn();
        int status = res.getResponse().getStatus();
        String body = res.getResponse().getContentAsString();
        if (status != 200) {
            throw new AssertionError("loginAndGetCookie expected 200 but got " + status + " body=" + body);
        }
        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).as("Set-Cookie present for login").isNotBlank();
        return setCookie.split(";", 2)[0];
    }

    @Test
    @DisplayName("PATCH /me updates user fields")
    void patchMeSuccess() throws Exception {
        String cookie = loginAndGetCookie("patch@me.com");
        mvc.perform(patch("/me")
                        .header("Cookie", cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"New Display\",\"bio\":\"Short bio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Display"))
                .andExpect(jsonPath("$.bio").value("Short bio"));
    }

    @Test
    @DisplayName("PATCH /me validation failure produces VALIDATION_ERROR envelope")
    void patchMeValidationFailure() throws Exception {
        String cookie = loginAndGetCookie("val@me.com");
        mvc.perform(patch("/me")
                        .header("Cookie", cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PATCH /me empty body rejected")
    void patchMeEmptyBody() throws Exception {
        String cookie = loginAndGetCookie("empty@me.com");
        mvc.perform(patch("/me").header("Cookie", cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Malformed SBSESSION cookie is ignored -> 401 UNAUTHENTICATED on /me")
    void malformedCookieUnauthorized() throws Exception {
        mvc.perform(get("/me").header("Cookie", "SBSESSION=not-a-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }
}
