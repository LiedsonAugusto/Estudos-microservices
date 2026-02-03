package com.estudo.schedulingService.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAppointmentRequest(

        @NotNull(message = "O ID do horário é obrigatório")
        UUID timeSlotId,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String notes) {
}
