package com.sameboat.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that health/info actuator endpoints are public at the default /actuator base path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicActuatorEndpointsIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /actuator/health is public")
    void actuatorHealth() throws Exception {
        mvc.perform(get("/actuator/health"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /actuator/info is public and exposes a version (build.version or info.version)")
    void actuatorInfo() throws Exception {
        var res = mvc.perform(get("/actuator/info"))
           .andExpect(status().isOk())
           .andReturn();
        String json = res.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        String buildVersion = node.at("/build/version").asText("");
        String rootVersion = node.path("version").asText("");
        String infoVersion = node.at("/info/version").asText("");
        assertThat(buildVersion.isEmpty() && rootVersion.isEmpty() && infoVersion.isEmpty())
                .as("one of build.version, version, or info.version should be present in /actuator/info")
                .isFalse();
    }
}
