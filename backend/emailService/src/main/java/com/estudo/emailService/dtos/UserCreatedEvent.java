package com.estudo.emailService.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String name,
        String email,
        LocalDateTime timestamp
) { }
