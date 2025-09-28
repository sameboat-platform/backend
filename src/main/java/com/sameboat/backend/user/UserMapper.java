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
}

