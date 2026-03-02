package com.estudo.userService.exceptions;

public class CpfAlreadyExistsException extends RuntimeException {
    public CpfAlreadyExistsException() {
        super("CPF já cadastrado");
    }
}
