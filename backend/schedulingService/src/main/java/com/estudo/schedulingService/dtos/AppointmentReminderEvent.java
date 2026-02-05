package com.estudo.schedulingService.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentReminderEvent(
        UUID appointmentId,
        String userEmail,
        String userName,
        String serviceName,
        LocalDate date,
        LocalTime time,
        String confirmationCode,
        LocalDateTime timestamp
) {

    public static AppointmentReminderEvent from(UUID appointmentId, String userEmail, String userName, String serviceName, LocalDate date, LocalTime time, String confirmationCode) {
        return new  AppointmentReminderEvent(
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
