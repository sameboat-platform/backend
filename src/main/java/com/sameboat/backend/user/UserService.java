package com.sameboat.backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // BEGIN secure registration / lookup additions
    public Optional<UserEntity> getByEmailNormalized(String rawEmail) {
        return repository.findByEmailIgnoreCase(normalizeEmail(rawEmail));
    }

    public UserEntity registerNew(String rawEmail, String rawPassword, PasswordEncoder encoder) {
        String email = normalizeEmail(rawEmail);
        repository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already registered"); });
        UserEntity e = new UserEntity();
        e.setEmail(email);
        e.setDisplayName(email);
        e.setPasswordHash(encoder.encode(rawPassword));
        return repository.save(e);
    }
    // END secure registration / lookup additions

    public Optional<UserEntity> findById(java.util.UUID id) { return repository.findById(id); }

    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    public boolean passwordMatches(UserEntity user, String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, user.getPasswordHash());
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public UserEntity updatePartial(UserEntity existing, UpdateUserRequest req) {
        if (req.displayName() != null) existing.setDisplayName(req.displayName());
        if (req.avatarUrl() != null) existing.setAvatarUrl(req.avatarUrl());
        if (req.bio() != null) existing.setBio(req.bio());
        if (req.timezone() != null) existing.setTimezone(req.timezone());
        return repository.save(existing);
    }
}
