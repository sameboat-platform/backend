package com.sameboat.backend.common;

import com.sameboat.backend.user.UserController;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Deprecated: superseded by GlobalExceptionHandlerNotFoundWebMvcTest using a dedicated
 * test controller. Kept here disabled to preserve any future reference.
 */
@WebMvcTest(controllers = UserController.class)
@Disabled("Superseded by GlobalExceptionHandlerNotFoundWebMvcTest")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /me returns 401 UNAUTHENTICATED when not authenticated (control)")
    void meWithoutAuth_is401() throws Exception {
        mvc.perform(get("/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }
}
