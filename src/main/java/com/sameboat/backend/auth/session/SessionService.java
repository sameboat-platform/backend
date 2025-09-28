package com.sameboat.backend.auth.session;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository repository;

    public SessionService(SessionRepository repository) {
        this.repository = repository;
    }

    public SessionEntity createSession(UUID userId, Duration ttl) {
        SessionEntity s = new SessionEntity();
        s.setUserId(userId);
        s.setExpiresAt(OffsetDateTime.now().plus(ttl));
        return repository.save(s);
    }

    public Optional<SessionEntity> findValid(UUID id) {
        return repository.findById(id)
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isAfter(OffsetDateTime.now()));
    }

    public Optional<SessionEntity> findById(UUID id) {
        return repository.findById(id);
    }

    public void touch(SessionEntity s) {
        s.setLastSeenAt(OffsetDateTime.now());
        repository.save(s);
    }

    public void invalidate(String token) {
        try {
            UUID id = UUID.fromString(token);
            repository.deleteById(id);
        } catch (IllegalArgumentException ignored) {
            // ignore invalid format
        }
    }
}
