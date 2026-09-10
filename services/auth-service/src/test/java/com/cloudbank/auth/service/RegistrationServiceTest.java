package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.outbox.UserContactOutboxService;
import com.cloudbank.auth.repository.AuthUserRepository;

import org.hibernate.exception.ConstraintViolationException;

import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    @Test
    void shouldNormalizeEmailHashPasswordSaveUserAndRecordContact() {
        AuthUserRepository authUserRepository =
                mock(
                        AuthUserRepository.class
                );

        PasswordEncoder passwordEncoder =
                mock(
                        PasswordEncoder.class
                );

        UserContactOutboxService outboxService =
                mock(
                        UserContactOutboxService.class
                );

        RegistrationService registrationService =
                new RegistrationService(
                        authUserRepository,
                        passwordEncoder,
                        outboxService
                );

        when(
                passwordEncoder.encode(
                        "StrongPass123"
                )
        ).thenReturn(
                "hashed-password"
        );

        when(
                authUserRepository.saveAndFlush(
                        any(AuthUser.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        AuthUser savedUser =
                registrationService.register(
                        "  User@Example.COM  ",
                        "StrongPass123"
                );

        assertEquals(
                "user@example.com",
                savedUser.getEmail()
        );

        assertEquals(
                "hashed-password",
                savedUser.getPasswordHash()
        );

        assertNotEquals(
                "StrongPass123",
                savedUser.getPasswordHash()
        );

        verify(
                passwordEncoder
        ).encode(
                "StrongPass123"
        );

        verify(
                authUserRepository
        ).saveAndFlush(
                any(AuthUser.class)
        );

        verify(
                outboxService
        ).recordRegistered(
                savedUser
        );
    }

    @Test
    void shouldNotPrecheckEmailAvailabilityBeforeHashingAndSaving() {
        AuthUserRepository authUserRepository =
                mock(
                        AuthUserRepository.class
                );

        PasswordEncoder passwordEncoder =
                mock(
                        PasswordEncoder.class
                );

        UserContactOutboxService outboxService =
                mock(
                        UserContactOutboxService.class
                );

        RegistrationService registrationService =
                new RegistrationService(
                        authUserRepository,
                        passwordEncoder,
                        outboxService
                );

        when(
                passwordEncoder.encode(
                        "StrongPass123"
                )
        ).thenReturn(
                "hashed-password"
        );

        when(
                authUserRepository.saveAndFlush(
                        any(AuthUser.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        registrationService.register(
                "  User@Example.COM  ",
                "StrongPass123"
        );

        verify(
                passwordEncoder
        ).encode(
                "StrongPass123"
        );

        verify(
                authUserRepository
        ).saveAndFlush(
                any(AuthUser.class)
        );

        verifyNoMoreInteractions(
                authUserRepository
        );

        verify(
                outboxService
        ).recordRegistered(
                any(AuthUser.class)
        );
    }

    @Test
    void shouldTranslateDatabaseDuplicateEmailConflict() {
        AuthUserRepository authUserRepository =
                mock(
                        AuthUserRepository.class
                );

        PasswordEncoder passwordEncoder =
                mock(
                        PasswordEncoder.class
                );

        UserContactOutboxService outboxService =
                mock(
                        UserContactOutboxService.class
                );

        RegistrationService registrationService =
                new RegistrationService(
                        authUserRepository,
                        passwordEncoder,
                        outboxService
                );

        when(
                passwordEncoder.encode(
                        "StrongPass123"
                )
        ).thenReturn(
                "hashed-password"
        );

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "duplicate email",
                        new SQLException(
                                "duplicate email",
                                "23505"
                        ),
                        "ux_auth_users_email_lower"
                );

        when(
                authUserRepository.saveAndFlush(
                        any(AuthUser.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate email",
                        constraintViolation
                )
        );

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> registrationService.register(
                        "user@example.com",
                        "StrongPass123"
                )
        );

        verify(
                outboxService,
                never()
        ).recordRegistered(
                any(AuthUser.class)
        );
    }

    @Test
    void shouldNotTranslateUnrelatedDatabaseIntegrityViolation() {
        AuthUserRepository authUserRepository =
                mock(
                        AuthUserRepository.class
                );

        PasswordEncoder passwordEncoder =
                mock(
                        PasswordEncoder.class
                );

        UserContactOutboxService outboxService =
                mock(
                        UserContactOutboxService.class
                );

        RegistrationService registrationService =
                new RegistrationService(
                        authUserRepository,
                        passwordEncoder,
                        outboxService
                );

        when(
                passwordEncoder.encode(
                        "StrongPass123"
                )
        ).thenReturn(
                "hashed-password"
        );

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "status constraint",
                        new SQLException(
                                "check violation",
                                "23514"
                        ),
                        "chk_auth_users_status"
                );

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "status constraint",
                        constraintViolation
                );

        when(
                authUserRepository.saveAndFlush(
                        any(AuthUser.class)
                )
        ).thenThrow(
                databaseException
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> registrationService.register(
                        "user@example.com",
                        "StrongPass123"
                )
        );

        verify(
                outboxService,
                never()
        ).recordRegistered(
                any(AuthUser.class)
        );
    }
}
