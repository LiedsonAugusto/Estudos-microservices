package com.estudo.schedulingService.exceptions;

public class InactiveServiceException extends RuntimeException {
    public InactiveServiceException(String message) {
        super(message);
    }
}
