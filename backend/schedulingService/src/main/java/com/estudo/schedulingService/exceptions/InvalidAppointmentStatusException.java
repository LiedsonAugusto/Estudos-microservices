package com.estudo.schedulingService.exceptions;

import com.estudo.schedulingService.enums.AppointmentStatus;

public class InvalidAppointmentStatusException extends RuntimeException {
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }

    public InvalidAppointmentStatusException(AppointmentStatus current, AppointmentStatus... required) {
        super(buildMessage(current, required));
    }

    private static String buildMessage(AppointmentStatus current, AppointmentStatus[] required) {
        StringBuilder sb = new StringBuilder("Operação inválida para o status atual: ")
                .append(current)
                .append(". Status esperado: ");
        for (int i = 0; i < required.length; i++) {
            sb.append(required[i]);
            if (i < required.length - 1) sb.append(" ou ");
        }
        return sb.toString();
    }
}
