package com.estudo.schedulingService.exceptions;

public class NoSlotsAvailableException extends RuntimeException {
    public NoSlotsAvailableException(String message) {
        super(message);
    }
}
