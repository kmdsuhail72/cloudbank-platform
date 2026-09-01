package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

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

        ensureEmailAvailable(normalizedEmail);

        String passwordHash = passwordEncoder.encode(rawPassword);

        AuthUser authUser = new AuthUser(
                normalizedEmail,
                passwordHash
        );

        return authUserRepository.save(authUser);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureEmailAvailable(String email) {
        if (authUserRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
    }
}
