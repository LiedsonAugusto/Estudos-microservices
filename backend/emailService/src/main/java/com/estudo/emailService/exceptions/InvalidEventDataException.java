package com.estudo.emailService.exceptions;

public class InvalidEventDataException extends RuntimeException {

    public InvalidEventDataException(String message) {
        super(message);
    }
}
