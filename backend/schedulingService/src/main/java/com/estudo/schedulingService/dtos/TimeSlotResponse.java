package com.estudo.schedulingService.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        ServiceResponse service,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer capacity,
        Integer availableSpots,
        boolean active) {
}
