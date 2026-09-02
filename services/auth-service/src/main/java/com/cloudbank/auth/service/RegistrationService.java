package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private static final String EMAIL_UNIQUE_CONSTRAINT =
            "ux_auth_users_email_lower";

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUser register(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);

        String passwordHash = passwordEncoder.encode(rawPassword);

        AuthUser authUser = new AuthUser(
                normalizedEmail,
                passwordHash
        );

        try {
            return authUserRepository.saveAndFlush(authUser);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateEmailViolation(exception)) {
                throw new EmailAlreadyRegisteredException(normalizedEmail);
            }

            throw exception;
        }
    }

    private boolean isDuplicateEmailViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && EMAIL_UNIQUE_CONSTRAINT.equals(
                            constraintViolation.getConstraintName()
                    )) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
