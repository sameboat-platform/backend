package com.sameboat.backend.auth.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository for persisted user sessions. Expiry filtering is
 * handled in {@link SessionService} to keep queries simple and portable.
 */
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    // expiry filtering handled in service to avoid DB-specific timestamp quirks
}
