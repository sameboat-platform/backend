package com.sameboat.backend.auth.dto;

import com.sameboat.backend.user.UserDto;

/**
 * Response returned upon successful authentication containing the user's
 * profile projection.
 * @param user authenticated user details
 */
public record LoginResponse(UserDto user) { }
