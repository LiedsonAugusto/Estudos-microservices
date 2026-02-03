package com.estudo.schedulingService.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateTimeSlotRequest(

        @NotNull(message = "O ID do serviço é obrigatório")
        UUID serviceId,

        @NotNull(message = "A data é obrigatória")
        @FutureOrPresent(message = "A data deve ser hoje ou no futuro")
        LocalDate date,

        @NotNull(message = "O horário de início é obrigatório")
        LocalTime startTime,

        @NotNull(message = "O horário de término é obrigatório")
        LocalTime endTime,

        @NotNull(message = "A capacidade é obrigatória")
        @Positive(message = "A capacidade deve ser maior que zero")
        @Max(value = 100, message = "A capacidade máxima é de 100 atendimentos")
        Integer capacity) {
}
