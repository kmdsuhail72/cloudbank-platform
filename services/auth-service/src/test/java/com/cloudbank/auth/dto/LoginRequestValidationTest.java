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

class LoginRequestValidationTest {

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
    void shouldAcceptValidLoginRequest() {
        LoginRequest request =
                new LoginRequest("user@example.com", "StrongPass123");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        LoginRequest request =
                new LoginRequest("invalid-email", "StrongPass123");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("email"))
        );
    }

    @Test
    void shouldRejectBlankPassword() {
        LoginRequest request =
                new LoginRequest("user@example.com", "");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("password"))
        );
    }

    @Test
    void shouldRejectPasswordExceedingBcryptByteLimit() {
        String password = "é".repeat(37);

        LoginRequest request =
                new LoginRequest("user@example.com", password);

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("password"))
        );
    }
}
