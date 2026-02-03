package com.estudo.schedulingService.exceptions;

public class ServiceAlreadyExistsException extends RuntimeException {

    public ServiceAlreadyExistsException(String serviceName) {
        super("Já existe um serviço com o nome: " + serviceName);
    }
}
