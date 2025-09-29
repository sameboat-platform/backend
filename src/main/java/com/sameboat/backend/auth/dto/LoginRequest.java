package com.sameboat.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request containing user credentials. Email is normalized by the service layer
 * (trim + lowercase) before lookup.
 * @param email user email (must exist unless dev auto-create enabled)
 * @param password raw password supplied by the user
 */
public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) { }
