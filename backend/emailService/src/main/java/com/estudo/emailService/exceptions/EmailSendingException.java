package com.estudo.emailService.exceptions;

public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String recipient, String eventType, Throwable cause) {
        super("Falha ao enviar email do tipo '" + eventType + "' para: " + recipient, cause);
    }
}
