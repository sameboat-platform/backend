package com.sameboat.backend.auth.session;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service encapsulating session lifecycle operations: creation, validation,
 * last-seen touch updates and invalidation. Uses a {@link java.time.Clock} to
 * ensure deterministic time handling and simplify testing.
 */
@Service
@Transactional
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository repository;
    private final Clock clock;

    public SessionService(SessionRepository repository, ObjectProvider<java.time.Clock> clockProvider) {
        this.repository = repository;
        this.clock = clockProvider.getIfAvailable(java.time.Clock::systemUTC);
    }

    /**
     * Creates and persists a new session for a user with a specified TTL.
     * @param userId user identifier
     * @param ttl time to live duration
     * @return persisted session entity
     */
    public SessionEntity createSession(UUID userId, Duration ttl) {
        SessionEntity s = new SessionEntity();
        s.setUserId(userId);
        s.setExpiresAt(OffsetDateTime.ofInstant(clock.instant().plus(ttl), ZoneOffset.UTC));
        SessionEntity saved = repository.save(s);
        repository.flush(); // ensure persistence before returning (visibility for immediate follow-up request)
        log.debug("Created session id={} userId={} expiresAt={} nowUTC={}", saved.getId(), userId, saved.getExpiresAt(), clock.instant());
        return saved;
    }

    /**
     * Finds a session by id and filters out those already expired at current clock instant.
     * @param id session uuid
     * @return optional valid session
     */
    public Optional<SessionEntity> findValid(UUID id) {
        Instant now = clock.instant();
        return repository.findById(id)
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().toInstant().isAfter(now));
    }

    /** Raw find without expiry filtering. */
    public Optional<SessionEntity> findById(UUID id) { return repository.findById(id); }

    /** Updates the lastSeen timestamp for activity tracking purposes. */
    public void touch(SessionEntity s) {
        s.setLastSeenAt(OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        repository.save(s);
    }

    /** Invalidates (deletes) a session if the token parses as a UUID. */
    public void invalidate(String token) {
        try {
            UUID id = UUID.fromString(token);
            repository.deleteById(id);
        } catch (IllegalArgumentException ignored) { }
    }

    /** Returns total stored sessions. */
    public long count() { return repository.count(); }
}
