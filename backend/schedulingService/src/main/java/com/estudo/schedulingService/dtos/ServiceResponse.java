package com.estudo.schedulingService.dtos;

import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        Integer durationMinutes,
        boolean active) {
}
