package com.sameboat.backend.auth;

import com.sameboat.backend.auth.session.SessionEntity;
import com.sameboat.backend.auth.session.SessionPruner;
import com.sameboat.backend.auth.session.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SessionPrunerIntegrationTest {

    @Autowired SessionRepository sessionRepository;
    @Autowired SessionPruner.SessionPruneService pruneService;

    @Test
    @DisplayName("SessionPruner deletes expired sessions and keeps valid ones")
    void prunesExpiredOnly() {
        var userId = UUID.randomUUID();
        var now = OffsetDateTime.now(ZoneOffset.UTC);

        // expired session
        SessionEntity expired = new SessionEntity();
        expired.setUserId(userId);
        expired.setExpiresAt(now.minusDays(1));
        expired.setLastSeenAt(now.minusDays(2));
        sessionRepository.save(expired);

        // valid session
        SessionEntity valid = new SessionEntity();
        valid.setUserId(userId);
        valid.setExpiresAt(now.plusDays(7));
        valid.setLastSeenAt(now);
        sessionRepository.save(valid);

        assertThat(sessionRepository.count()).isEqualTo(2);
        long deleted = pruneService.pruneNow();
        assertThat(deleted).isEqualTo(1);
        assertThat(sessionRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.findById(valid.getId())).isPresent();
    }
}

