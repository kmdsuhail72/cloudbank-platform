package com.cloudbank.auth.service;

import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    @Test
    void shouldAuthenticateActiveUserWithValidCredentials() {
        AuthUserRepository authUserRepository =
                mock(AuthUserRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        LoginService loginService =
                new LoginService(
                        authUserRepository,
                        passwordEncoder
                );

        AuthUser authUser =
                new AuthUser(
                        "user@example.com",
                        "hashed-password"
                );

        authUser.recordFailedLoginAttempt();

        when(authUserRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(authUser));

        when(passwordEncoder.matches(
                "StrongPass123",
                "hashed-password"
        )).thenReturn(true);

        AuthUser authenticatedUser =
                loginService.login(
                        "  User@Example.COM  ",
                        "StrongPass123"
                );

        assertSame(authUser, authenticatedUser);
        assertEquals(
                0,
                authenticatedUser.getFailedLoginAttempts()
        );

        verify(authUserRepository)
                .findByEmailIgnoreCase("user@example.com");

        verify(passwordEncoder)
                .matches(
                        "StrongPass123",
                        "hashed-password"
                );
    }
}
