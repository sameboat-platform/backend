package com.sameboat.backend.auth;

import java.util.UUID;

public record AuthPrincipal(UUID userId, String email, String role) { }
