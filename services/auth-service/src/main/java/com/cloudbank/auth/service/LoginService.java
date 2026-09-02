package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.InvalidCredentialsException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUser login(String email, String rawPassword) {
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        AuthUser authUser = authUserRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                rawPassword,
                authUser.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        authUser.resetFailedLoginAttempts();

        return authUser;
    }
}
