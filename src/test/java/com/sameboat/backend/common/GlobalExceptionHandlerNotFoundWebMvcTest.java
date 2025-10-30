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
 * Verifies that throwing ResourceNotFoundException from a controller results in
 * a 404 response with error code NOT_FOUND handled by GlobalExceptionHandler.
 */
@WebMvcTest(controllers = TestNotFoundController.class)
class GlobalExceptionHandlerNotFoundWebMvcTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser // bypass security so request reaches controller
    @DisplayName("ResourceNotFoundException -> 404 NOT_FOUND envelope")
    void notFoundMapped() throws Exception {
        mvc.perform(get("/test/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}

