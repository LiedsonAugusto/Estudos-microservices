package com.estudo.schedulingService.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(

        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        @Size(min = 10, max = 500, message = "O motivo deve ter entre 10 e 500 caracteres")
        String reason) {
}
