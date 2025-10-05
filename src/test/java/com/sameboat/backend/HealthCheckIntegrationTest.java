package com.sameboat.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.endpoints.web.base-path=/api/actuator"
})
@ActiveProfiles("test") // <-- IMPORTANT
class HealthCheckIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void actuatorHealthEndpointShouldReturnUp() {
        ResponseEntity<String> res =
                rest.getForEntity("http://localhost:" + port + "/api/actuator/health", String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).contains("UP");
    }

    @Test
    void customHealthEndpointShouldReturnOk() {
        ResponseEntity<String> res =
                rest.getForEntity("http://localhost:" + port + "/health", String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).contains("ok");
    }
}