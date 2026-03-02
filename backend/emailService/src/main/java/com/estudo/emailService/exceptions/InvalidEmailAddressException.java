package com.estudo.emailService.exceptions;

public class InvalidEmailAddressException extends InvalidEventDataException {

    public InvalidEmailAddressException(String email) {
        super("Endereço de email inválido: '" + email + "'");
    }
}
