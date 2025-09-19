package com.sameboat.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")   // <-- use the test profile above
class SameboatBackendApplicationTests {

    @Test
    void contextLoads() {
        // smoke test: just verifies the app context (without DB/Flyway) starts
    }
}
