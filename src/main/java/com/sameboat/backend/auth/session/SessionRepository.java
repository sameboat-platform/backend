package com.sameboat.backend.auth.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    // expiry filtering handled in service to avoid DB-specific timestamp quirks
}
