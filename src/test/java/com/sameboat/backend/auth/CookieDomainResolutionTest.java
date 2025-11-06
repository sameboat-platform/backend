package com.sameboat.backend.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "sameboat.cookie.secure=true",
        "sameboat.cookie.domain=", // force dynamic resolution
        "sameboat.cookie.valid-domains[0]=sameboatplatform.com"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CookieDomainResolutionTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("When X-Forwarded-Host ends with apex, Set-Cookie contains Domain=apex (no dot)")
    void forwardedHostApexDomain() throws Exception {
        String email = "cookietest1@example.com";
        mvc.perform(post("/auth/register")
                        .header("X-Forwarded-Host", "sameboatplatform.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"GoodPass9!\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andDo(res -> {
                    String cookie = res.getResponse().getHeader("Set-Cookie");
                    assertThat(cookie).contains("Domain=sameboatplatform.com");
                    assertThat(cookie).doesNotContain("Domain=.sameboatplatform.com");
                });
    }

    @Test
    @DisplayName("When host is non-matching, Set-Cookie omits Domain (host-only)")
    void nonMatchingHostHostOnly() throws Exception {
        String email = "cookietest2@example.com";
        mvc.perform(post("/auth/register")
                        .header("X-Forwarded-Host", "api-sameboat.onrender.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"GoodPass9!\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andDo(res -> {
                    String cookie = res.getResponse().getHeader("Set-Cookie");
                    assertThat(cookie).doesNotContain("Domain=");
                });
    }
}

