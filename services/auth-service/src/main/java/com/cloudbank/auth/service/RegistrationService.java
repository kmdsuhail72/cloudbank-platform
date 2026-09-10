package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.outbox.UserContactOutboxService;
import com.cloudbank.auth.repository.AuthUserRepository;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class RegistrationService {

    private static final String EMAIL_UNIQUE_CONSTRAINT =
            "ux_auth_users_email_lower";

    private final AuthUserRepository authUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserContactOutboxService
            userContactOutboxService;

    public RegistrationService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            UserContactOutboxService userContactOutboxService
    ) {
        this.authUserRepository =
                Objects.requireNonNull(
                        authUserRepository
                );

        this.passwordEncoder =
                Objects.requireNonNull(
                        passwordEncoder
                );

        this.userContactOutboxService =
                Objects.requireNonNull(
                        userContactOutboxService
                );
    }

    @Transactional
    public AuthUser register(
            String email,
            String rawPassword
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        String passwordHash =
                passwordEncoder.encode(
                        rawPassword
                );

        AuthUser authUser =
                new AuthUser(
                        normalizedEmail,
                        passwordHash
                );

        try {
            AuthUser persisted =
                    authUserRepository.saveAndFlush(
                            authUser
                    );

            /*
             * AuthUser receives its UUID from @PrePersist.
             * saveAndFlush() therefore establishes the
             * authoritative user ID before the outbox row.
             *
             * Both writes remain inside this transaction.
             */
            userContactOutboxService
                    .recordRegistered(
                            persisted
                    );

            return persisted;

        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateEmailViolation(
                    exception
            )) {
                throw new EmailAlreadyRegisteredException(
                        normalizedEmail
                );
            }

            throw exception;
        }
    }

    private boolean isDuplicateEmailViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause =
                exception;

        while (cause != null) {
            if (cause instanceof
                    ConstraintViolationException constraintViolation
                    && EMAIL_UNIQUE_CONSTRAINT.equals(
                            constraintViolation
                                    .getConstraintName()
                    )) {
                return true;
            }

            cause =
                    cause.getCause();
        }

        return false;
    }

    private String normalizeEmail(
            String email
    ) {
        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}
