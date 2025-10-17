package com.sameboat.backend.auth.dto;

/**
 * Registration request payload containing email, password and optional display name.
 * Validation annotations enforce format and length constraints.
 * @param email user email (unique, validated format)
 * @param password raw password (8-100 chars, must include upper, lower, digit)
 * @param displayName optional display name (2-50 chars)
 */
public record RegisterRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 8, max = 100)
        @jakarta.validation.constraints.Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "password must be at least 8 characters and include upper, lower, and digit")
        String password,
        @jakarta.validation.constraints.Size(min = 2, max = 50) String displayName
) {}
