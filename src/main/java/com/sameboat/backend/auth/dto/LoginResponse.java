package com.sameboat.backend.auth.dto;

import com.sameboat.backend.user.UserDto;

public record LoginResponse(UserDto user) { }

