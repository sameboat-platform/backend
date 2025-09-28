package com.sameboat.backend.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 50) String displayName,
        @Size(max = 255) String avatarUrl,
        @Size(max = 500) String bio,
        @Size(max = 100) String timezone
) { }

