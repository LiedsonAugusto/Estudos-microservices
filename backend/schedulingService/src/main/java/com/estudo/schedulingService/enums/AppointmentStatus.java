package com.estudo.schedulingService.enums;

public enum AppointmentStatus {
    SCHEDULED,   // Agendado, aguardando
    CONFIRMED,   // Cidadão fez check-in
    COMPLETED,   // Atendimento realizado
    CANCELLED,   // Cancelado pelo cidadão
    NO_SHOW      // Cidadão não compareceu
}
