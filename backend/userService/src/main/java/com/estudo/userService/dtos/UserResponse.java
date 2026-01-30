package com.estudo.userService.dtos;

import com.estudo.userService.enums.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        UserRole role,
        LocalDateTime createdAt
) {}
