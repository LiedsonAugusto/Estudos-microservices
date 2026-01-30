package com.estudo.userService.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Telefone é obrigatório")
        String phone
) {}