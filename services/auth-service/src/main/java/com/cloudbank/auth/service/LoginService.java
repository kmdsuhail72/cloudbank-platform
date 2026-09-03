package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.InvalidCredentialsException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.model.UserStatus;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class LoginService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    private static final String DUMMY_PASSWORD_HASH =
            "$2y$10$2ar0dx96iNoH4U55xnEGRODKKWPLS1ZGG9D9oCUJyR3rJ1ysNRCau";

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public AuthUser login(String email, String rawPassword) {
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        Optional<AuthUser> authUserOptional = authUserRepository
                .findByEmailIgnoreCaseForUpdate(normalizedEmail);

        if (authUserOptional.isEmpty()) {
            passwordEncoder.matches(
                    rawPassword,
                    DUMMY_PASSWORD_HASH
            );

            throw new InvalidCredentialsException();
        }

        AuthUser authUser = authUserOptional.get();

        boolean passwordMatches = passwordEncoder.matches(
                rawPassword,
                authUser.getPasswordHash()
        );

        if (authUser.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        if (!passwordMatches) {
            authUser.recordFailedLoginAttempt();

            if (authUser.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                authUser.lock();
            }

            throw new InvalidCredentialsException();
        }

        authUser.resetFailedLoginAttempts();

        return authUser;
    }
}
