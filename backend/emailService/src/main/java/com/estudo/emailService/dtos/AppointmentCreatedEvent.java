package com.estudo.emailService.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentCreatedEvent(
        UUID appointmentId,
        String userEmail,
        String userName,
        String serviceName,
        LocalDate date,
        LocalTime time,
        String confirmationCode,
        LocalDateTime timestamp
) {}
