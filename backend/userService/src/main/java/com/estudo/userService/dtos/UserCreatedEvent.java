package com.estudo.userService.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreatedEvent(
    UUID userId,
    String name,
    String email,
    LocalDateTime timestamp
) {

    public static UserCreatedEvent from(UUID userId, String name, String email) {
        return new UserCreatedEvent(
            userId,
            name,
            email,
            LocalDateTime.now()
        );
    }
}
