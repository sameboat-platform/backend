package com.sameboat.backend.user;

import java.util.UUID;

/**
 * Public-facing user profile DTO with no sensitive fields (no email).
 * Stubbed out for future use when public user profiles are exposed = Post-MVP.
 */
public record PublicUserDto(UUID id, String displayName, String avatarUrl, String bio, String timezone) { }

