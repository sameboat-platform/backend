package com.sameboat.backend.auth.session;

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

    private final SessionPruneService sessionPruneService;

    public SessionPruner(SessionPruneService sessionPruneService) {
        this.sessionPruneService = sessionPruneService;
    }

    /**
     * Prunes expired sessions from the database.
     * Runs every hour, starting 2 minutes after application start.
     * // TODO: Externalize schedule timing to application.yml if environment tuning needed
     */
    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT2M")
    public void scheduledPrune() {
        sessionPruneService.pruneNow();
    }

    /**
     * Service to handle transactional session pruning.
     */
    @Component
    public static class SessionPruneService {
        private final SessionRepository sessionRepository;

        public SessionPruneService(SessionRepository sessionRepository) {
            this.sessionRepository = sessionRepository;
        }

        @Transactional
        public long pruneNow() {
            var now = OffsetDateTime.now(ZoneOffset.UTC);
            return sessionRepository.deleteExpiredSessions(now);
        }
    }
}
