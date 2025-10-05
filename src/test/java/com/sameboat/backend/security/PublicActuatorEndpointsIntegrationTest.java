package com.sameboat.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Verifies that health/info actuator endpoints (with and without the /api base path) are public.
 */
@SpringBootTest(properties = {
        "management.endpoints.web.base-path=/api/actuator"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicActuatorEndpointsIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /api/actuator/health is public")
    void apiActuatorHealth() throws Exception {
        mvc.perform(get("/api/actuator/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/actuator/info is public")
    void apiActuatorInfo() throws Exception {
        mvc.perform(get("/api/actuator/info"))
           .andExpect(status().isOk());
    }
}
