package com.estudo.schedulingService.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentCancelledEvent(
        UUID appointmentId,
        String userEmail,
        String userName,
        String serviceName,
        LocalDate date,
        LocalTime time,
        String reason,
        LocalDateTime timestamp
) {

    public static AppointmentCancelledEvent from(UUID appointmentId, String userEmail, String userName, String serviceName, LocalDate date, LocalTime time, String reason) {
        return new  AppointmentCancelledEvent(
                appointmentId,
                userEmail,
                userName,
                serviceName,
                date,
                time,
                reason,
                LocalDateTime.now()
        );
    }
}
