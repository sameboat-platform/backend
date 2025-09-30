package com.sameboat.backend.auth.dto;

/**
 * Registration request payload containing email, password and optional display name.
 * Validation annotations enforce format and length constraints.
 * @param email user email (unique, validated format)
 * @param password raw password (6-100 chars)
 * @param displayName optional display name (2-50 chars)
 */
public record RegisterRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 6, max = 100) String password,
        @jakarta.validation.constraints.Size(min = 2, max = 50) String displayName
) {}
