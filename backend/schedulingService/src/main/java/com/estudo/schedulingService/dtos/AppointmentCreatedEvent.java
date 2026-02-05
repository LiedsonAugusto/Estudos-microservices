package com.estudo.schedulingService.dtos;

import com.estudo.schedulingService.entities.TimeSlot;

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
) {

    public static AppointmentCreatedEvent from(UUID appointmentId, String userEmail, String userName, String serviceName, LocalDate date, LocalTime time, String confirmationCode) {
        return new AppointmentCreatedEvent(
                appointmentId,
                userEmail,
                userName,
                serviceName,
                date,
                time,
                confirmationCode,
                LocalDateTime.now()
        );

    }
}
