package com.sameboat.backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.sameboat.backend.common.ResourceNotFoundException;

/**
 * Application service providing user-centric operations such as registration,
 * credential verification, profile partial updates and normalized lookups.
 * <p>
 * All write operations are wrapped in a transactional context. Password hashing
 * is delegated to the provided {@link PasswordEncoder} to keep concerns separated.
 */
@Service
@Transactional
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Looks up a user by email after applying normalization (trim + lowercase).
     * @param rawEmail raw email input
     * @return user if found
     */
    public Optional<UserEntity> getByEmailNormalized(String rawEmail) {
        return repository.findByEmailIgnoreCase(normalizeEmail(rawEmail));
    }

    /**
     * Registers a new user ensuring email uniqueness (case-insensitive).
     * @param rawEmail email provided by client
     * @param rawPassword plaintext password (will be encoded)
     * @param encoder encoder to hash the password
     * @return persisted user entity
     * @throws IllegalArgumentException if email already exists
     */
    public UserEntity registerNew(String rawEmail, String rawPassword, PasswordEncoder encoder) {
        String email = normalizeEmail(rawEmail);
        repository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already registered"); });
        UserEntity e = new UserEntity();
        e.setEmail(email);
        e.setDisplayName(email);
        e.setPasswordHash(encoder.encode(rawPassword));
        return repository.save(e);
    }

    /** Finds a user by id. */
    public Optional<UserEntity> findById(UUID id) { return repository.findById(id); }

    /** Gets a user by id or throws ResourceNotFoundException. */
    public UserEntity getByIdOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** Case-insensitive email lookup (expects already-normalized input). */
    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    /**
     * Verifies a raw password against the stored hash.
     * @param user user whose hash to compare
     * @param rawPassword password to test
     * @param encoder encoder used for hashing
     * @return true if passwords match
     */
    public boolean passwordMatches(UserEntity user, String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, user.getPasswordHash());
    }

    /** Normalizes an email (trim + lowercase, null-safe). */
    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Applies a partial update to mutable profile fields if they are present in the request.
     * @param existing persistent entity loaded from repository
     * @param req DTO carrying optional new values
     * @return updated and saved entity
     */
    public UserEntity updatePartial(UserEntity existing, UpdateUserRequest req) {
        if (req.displayName() != null) existing.setDisplayName(req.displayName());
        if (req.avatarUrl() != null) existing.setAvatarUrl(req.avatarUrl());
        if (req.bio() != null) existing.setBio(req.bio());
        if (req.timezone() != null) existing.setTimezone(req.timezone());
        return repository.save(existing);
    }
}
