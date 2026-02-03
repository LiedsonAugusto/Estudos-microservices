package com.estudo.schedulingService.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(Long id) {
        super("Serviço não encontrado com ID: " + id);
    }
}
