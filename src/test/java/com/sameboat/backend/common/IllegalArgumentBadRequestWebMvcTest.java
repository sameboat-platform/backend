package com.sameboat.backend.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused test verifying IllegalArgumentException is mapped to BAD_REQUEST envelope.
 */
@WebMvcTest(controllers = ThrowingBadRequestController.class)
class IllegalArgumentBadRequestWebMvcTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser
    @DisplayName("IllegalArgumentException -> 400 BAD_REQUEST envelope")
    void illegalArgument_mapped() throws Exception {
        mvc.perform(get("/test/badrequest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
}
