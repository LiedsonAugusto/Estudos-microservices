package com.estudo.userService.dtos;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {}

