package com.estudo.userService.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<ValidCPF, String> {

    @Override
    public void initialize(ValidCPF constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        // Se o CPF for null ou vazio, considera inválido
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        // o \\D é para não númericos, tudo que não for númerico será retirado
        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        // olha se todos os números do cpf sao iguais, invalido
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstVerifier = 11 - (sum % 11);
        if (firstVerifier >= 10) {
            firstVerifier = 0;
        }

        if (firstVerifier != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondVerifier = 11 - (sum % 11);
        if (secondVerifier >= 10) {
            secondVerifier = 0;
        }

        return secondVerifier == Character.getNumericValue(cpf.charAt(10));
    }
}