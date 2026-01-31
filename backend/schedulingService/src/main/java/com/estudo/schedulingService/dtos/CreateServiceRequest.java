package com.estudo.schedulingService.dtos;

import jakarta.validation.constraints.*;

public record CreateServiceRequest(

        @NotBlank(message = "O serviço deve possuir um nome")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,

        @NotBlank(message = "O serviço deve possuir uma descrição")
        @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
        String description,

        @NotNull(message = "O serviço deve possuir uma duração em minutos")
        @Positive(message = "A duração deve ser maior que zero")
        @Max(value = 480, message = "A duração máxima é de 480 minutos (8 horas)")
        Integer durationMinutes) {
}
