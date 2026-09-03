package com.cloudbank.auth.service;

import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RegistrationServiceIntegrationTest {

    private static final String TEST_EMAIL =
            "registration-race-test@cloudbank.local";

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void cleanBeforeTest() {
        deleteTestUser();
    }

    @AfterEach
    void cleanAfterTest() {
        deleteTestUser();
    }

    @Test
    void shouldExposeEmailUniqueIndexNameForDatabaseConflict() {
        AuthUser firstUser = new AuthUser(
                TEST_EMAIL,
                passwordEncoder.encode("StrongPass123")
        );

        authUserRepository.saveAndFlush(firstUser);

        AuthUser duplicateUser = new AuthUser(
                TEST_EMAIL.toUpperCase(),
                passwordEncoder.encode("StrongPass456")
        );

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> authUserRepository.saveAndFlush(duplicateUser)
        );

        ConstraintViolationException constraintViolation =
                findConstraintViolation(exception);

        assertEquals(
                "ux_auth_users_email_lower",
                constraintViolation.getConstraintName()
        );
    }

    private ConstraintViolationException findConstraintViolation(
            Throwable exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                return constraintViolation;
            }

            cause = cause.getCause();
        }

        throw new AssertionError(
                "Expected Hibernate ConstraintViolationException in cause chain"
        );
    }

    private void deleteTestUser() {
        authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(authUserRepository::delete);
    }
}
