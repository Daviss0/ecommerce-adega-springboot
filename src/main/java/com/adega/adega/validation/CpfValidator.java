package com.adega.adega.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {

        if (cpf == null || cpf.isBlank()) {
            return true;
        }

        String normalizedCpf = cpf.replaceAll("\\D", "");

        if(normalizedCpf.length() != 11) {
            return false;
        }

        if(hasRepeatedDigits(normalizedCpf)) {
            return false;
        }

        int firstDigit = calculateVerificationDigit(normalizedCpf.substring(0, 9), 10);

        int secondDigit = calculateVerificationDigit(normalizedCpf.substring(0, 9) + firstDigit, 11);

        int informedFirstDigit = Character.getNumericValue(normalizedCpf.charAt(9));

        int informedSecondDigit = Character.getNumericValue(normalizedCpf.charAt(10));

        return firstDigit == informedFirstDigit && secondDigit == informedSecondDigit;
    }

    private int calculateVerificationDigit(String base, int initialWeight) {
        int sum = 0;
        int weight = initialWeight;

        for (int index = 0; index < base.length(); index++) {
            int digit = Character.getNumericValue(base.charAt(index));

            sum += digit * weight;
            weight --;
        }

        int remainder = sum % 11;

        if(remainder < 2) {
            return 0;
        }

        return 11 - remainder;
    }

    private boolean hasRepeatedDigits (String cpf) {
        return cpf.chars().distinct().count() == 1;
    }
}
