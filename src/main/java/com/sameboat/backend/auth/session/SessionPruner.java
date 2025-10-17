package com.sameboat.backend.auth.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Periodic job that deletes expired sessions from the database. Runs at a
 * fixed delay; cron can be adjusted later if needed. Exposes a pruneNow()
 * method for tests and ad-hoc invocations.
 */
@Component
public class SessionPruner {
    private static final Logger log = LoggerFactory.getLogger(SessionPruner.class);

    private final SessionRepository sessionRepository;
    // Lazily-injected self proxy to ensure @Transactional is applied when invoked from scheduled method
    private final ObjectProvider<SessionPruner> self;

    public SessionPruner(SessionRepository sessionRepository, @Lazy ObjectProvider<SessionPruner> self) {
        this.sessionRepository = sessionRepository;
        this.self = self;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT2M")
    public void scheduledPrune() {
        // Call through proxy so @Transactional on pruneNow() is honored
        self.getObject().pruneNow();
    }

    @Transactional
    public long pruneNow() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        long deleted = sessionRepository.deleteExpiredSessions(now);
        if (deleted > 0) {
            log.info("SessionPruner deleted {} expired sessions", deleted);
        }
        return deleted;
    }
}
