package com.sameboat.backend.auth;

import com.sameboat.backend.auth.session.SessionEntity;
import com.sameboat.backend.auth.session.SessionRepository;
import com.sameboat.backend.auth.session.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SessionService.class)
class SessionServiceTest {

    @Autowired
    SessionService sessionService;
    @Autowired
    SessionRepository sessionRepository;

    @Test
    void createSessionAndFindValid() {
        UUID userId = UUID.randomUUID();
        var s = sessionService.createSession(userId, Duration.ofMinutes(5));
        assertThat(s.getId()).isNotNull();
        assertThat(sessionService.findValid(s.getId())).isPresent();
    }

    @Test
    void expiredSessionNotReturned() {
        UUID userId = UUID.randomUUID();
        var s = sessionService.createSession(userId, Duration.ofMinutes(5));
        // force expire
        SessionEntity stored = sessionRepository.findById(s.getId()).orElseThrow();
        stored.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        sessionRepository.save(stored);
        assertThat(sessionService.findValid(s.getId())).isEmpty();
    }

    @Test
    void invalidateWithBadTokenNoThrow() {
        sessionService.invalidate("not-a-uuid");
        // nothing to assert; just ensure no exception
    }
}

