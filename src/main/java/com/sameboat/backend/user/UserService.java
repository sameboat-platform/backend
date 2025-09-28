package com.sameboat.backend.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserEntity findOrCreateByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).orElseGet(() -> {
            UserEntity e = new UserEntity();
            e.setEmail(email);
            e.setDisplayName(email);
            e.setPasswordHash("DEV-STUB");
            return repository.save(e);
        });
    }

    public Optional<UserEntity> findById(java.util.UUID id) { return repository.findById(id); }

    public UserEntity updatePartial(UserEntity existing, UpdateUserRequest req) {
        if (req.displayName() != null) existing.setDisplayName(req.displayName());
        if (req.avatarUrl() != null) existing.setAvatarUrl(req.avatarUrl());
        if (req.bio() != null) existing.setBio(req.bio());
        if (req.timezone() != null) existing.setTimezone(req.timezone());
        return repository.save(existing);
    }
}

