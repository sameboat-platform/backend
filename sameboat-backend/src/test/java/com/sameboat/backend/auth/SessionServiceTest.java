package com.sameboat.backend.auth;
// ...existing code...
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
// ...existing code...
@DataJpaTest
@Import(SessionService.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "sameboat.security.enabled=false")
class SessionServiceTest {
// ...existing code...

