package com.sameboat.backend.user;

import java.util.UUID;

public record UserDto(UUID id, String email, String displayName, String avatarUrl, String bio, String timezone, String role) { }

