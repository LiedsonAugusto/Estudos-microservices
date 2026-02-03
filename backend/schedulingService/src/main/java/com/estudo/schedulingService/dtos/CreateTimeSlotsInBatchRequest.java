package com.estudo.schedulingService.dtos;

import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateTimeSlotsInBatchRequest(

        @NotNull(message = "O ID do serviço é obrigatório")
        UUID serviceId,

        @NotNull(message = "A data inicial é obrigatória")
        @FutureOrPresent(message = "A data inicial deve ser hoje ou no futuro")
        LocalDate startDate,

        @NotNull(message = "A data final é obrigatória")
        @FutureOrPresent(message = "A data final deve ser hoje ou no futuro")
        LocalDate endDate,

        @NotNull(message = "Os dias da semana são obrigatórios")
        @NotEmpty(message = "Deve haver pelo menos um dia da semana")
        List<DayOfWeek> daysOfWeek,

        @NotNull(message = "O horário de início é obrigatório")
        LocalTime startTime,

        @NotNull(message = "O horário de término é obrigatório")
        LocalTime endTime,

        @NotNull(message = "A duração do slot é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        @Max(value = 480, message = "A duração máxima é de 480 minutos (8 horas)")
        Integer slotDurationMinutes,

        @NotNull(message = "A capacidade é obrigatória")
        @Positive(message = "A capacidade deve ser maior que zero")
        @Max(value = 100, message = "A capacidade máxima é de 100 atendimentos")
        Integer capacity) {
}
