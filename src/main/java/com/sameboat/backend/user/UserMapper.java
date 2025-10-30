package com.sameboat.backend.user;

public final class UserMapper {
    private UserMapper() {}

    public static UserDto toDto(UserEntity e) {
        if (e == null) return null;
        return new UserDto(
                e.getId(),
                e.getEmail(),
                e.getDisplayName(),
                e.getAvatarUrl(),
                e.getBio(),
                e.getTimezone(),
                e.getRole()
        );
    }

    // Map to public DTO (without email and role)--stubbed for future use
    public static PublicUserDto toPublicDto(UserEntity e) {
        if (e == null) return null;
        return new PublicUserDto(
                e.getId(),
                e.getDisplayName(),
                e.getAvatarUrl(),
                e.getBio(),
                e.getTimezone()
        );
    }
}
