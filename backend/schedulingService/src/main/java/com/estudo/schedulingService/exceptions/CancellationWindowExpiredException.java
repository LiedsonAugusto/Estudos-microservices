package com.estudo.schedulingService.exceptions;

public class CancellationWindowExpiredException extends RuntimeException {
    public CancellationWindowExpiredException(String message) {
        super(message);
    }
}
