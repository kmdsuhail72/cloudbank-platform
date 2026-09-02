package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.InvalidCredentialsException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void shouldRejectLockedUserEvenWithCorrectPassword() {
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

        authUser.lock();

        when(authUserRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(authUser));

        when(passwordEncoder.matches(
                "StrongPass123",
                "hashed-password"
        )).thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "user@example.com",
                        "StrongPass123"
                )
        );
    }

    @Test
    void shouldRecordFailedLoginAttemptWhenPasswordIsIncorrect() {
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

        when(authUserRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(authUser));

        when(passwordEncoder.matches(
                "WrongPass123",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "user@example.com",
                        "WrongPass123"
                )
        );

        assertEquals(
                1,
                authUser.getFailedLoginAttempts()
        );
    }

    @Test
    void shouldLockAccountAfterFifthFailedLoginAttempt() {
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
        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();

        when(authUserRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(authUser));

        when(passwordEncoder.matches(
                "WrongPass123",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "user@example.com",
                        "WrongPass123"
                )
        );

        assertEquals(
                5,
                authUser.getFailedLoginAttempts()
        );

        assertEquals(
                com.cloudbank.auth.model.UserStatus.LOCKED,
                authUser.getStatus()
        );
    }

    @Test
    void shouldPerformPasswordComparisonWhenEmailDoesNotExist() {
        AuthUserRepository authUserRepository =
                mock(AuthUserRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        LoginService loginService =
                new LoginService(
                        authUserRepository,
                        passwordEncoder
                );

        when(authUserRepository.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "missing@example.com",
                        "StrongPass123"
                )
        );

        verify(passwordEncoder)
                .matches(
                        eq("StrongPass123"),
                        anyString()
                );
    }

}
