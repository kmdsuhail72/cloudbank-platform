package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    @Test
    void shouldNormalizeEmailHashPasswordAndSaveUser() {
        AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        RegistrationService registrationService =
                new RegistrationService(authUserRepository, passwordEncoder);

        when(authUserRepository.existsByEmailIgnoreCase("user@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("StrongPass123"))
                .thenReturn("hashed-password");

        when(authUserRepository.save(any(AuthUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthUser savedUser = registrationService.register(
                "  User@Example.COM  ",
                "StrongPass123"
        );

        assertEquals("user@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertNotEquals("StrongPass123", savedUser.getPasswordHash());

        verify(authUserRepository)
                .existsByEmailIgnoreCase("user@example.com");

        verify(passwordEncoder)
                .encode("StrongPass123");

        verify(authUserRepository)
                .save(any(AuthUser.class));
    }

    @Test
    void shouldRejectAlreadyRegisteredEmail() {
        AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        RegistrationService registrationService =
                new RegistrationService(authUserRepository, passwordEncoder);

        when(authUserRepository.existsByEmailIgnoreCase("user@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> registrationService.register(
                        "  User@Example.COM  ",
                        "StrongPass123"
                )
        );

        verify(authUserRepository)
                .existsByEmailIgnoreCase("user@example.com");

        verify(passwordEncoder, never())
                .encode(any());

        verify(authUserRepository, never())
                .save(any(AuthUser.class));
    }
}
