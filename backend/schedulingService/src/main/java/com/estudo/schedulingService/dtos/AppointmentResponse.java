package com.estudo.schedulingService.dtos;

import com.estudo.schedulingService.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID userId,
        String userName,
        TimeSlotResponse timeSlot,
        AppointmentStatus status,
        String notes,
        String confirmationCode,
        LocalDateTime createdAt) {
}
