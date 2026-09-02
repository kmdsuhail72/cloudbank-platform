package com.cloudbank.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class BcryptPasswordValidator
        implements ConstraintValidator<BcryptPassword, String> {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null) {
            return true;
        }

        return password
                .getBytes(StandardCharsets.UTF_8)
                .length <= MAX_BCRYPT_PASSWORD_BYTES;
    }
}
