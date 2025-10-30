package com.sameboat.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration request payload containing email, password and optional display name.
 * Validation annotations enforce format and length constraints.
 * @param email user email (unique, validated format)
 * @param password raw password (8-100 chars, must include upper, lower, digit)
 * @param displayName optional display name (2-50 chars)
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "password must be at least 8 characters and include upper, lower, and digit")
        String password,
        @Size(min = 2, max = 50) String displayName
) {}
