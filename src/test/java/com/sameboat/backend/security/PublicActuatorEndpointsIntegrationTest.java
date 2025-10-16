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
 * Verifies that health/info actuator endpoints are public at the default /actuator base path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicActuatorEndpointsIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /actuator/health is public")
    void actuatorHealth() throws Exception {
        mvc.perform(get("/actuator/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/info is public and returns version field")
    void actuatorInfo() throws Exception {
        mvc.perform(get("/actuator/info"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.build.version").exists());
    }
}
