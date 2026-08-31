package com.cloudbank.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidRegistrationRequest() {
        RegisterRequest request =
                new RegisterRequest("user@example.com", "StrongPass123");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        RegisterRequest request =
                new RegisterRequest("invalid-email", "StrongPass123");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("email"))
        );
    }

    @Test
    void shouldRejectShortPassword() {
        RegisterRequest request =
                new RegisterRequest("user@example.com", "short");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("password"))
        );
    }
}
