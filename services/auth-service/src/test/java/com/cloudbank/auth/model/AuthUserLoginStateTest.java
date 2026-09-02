package com.cloudbank.auth.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthUserLoginStateTest {

    @Test
    void shouldIncrementFailedLoginAttempts() {
        AuthUser authUser =
                new AuthUser("user@example.com", "hashed-password");

        authUser.recordFailedLoginAttempt();

        assertEquals(1, authUser.getFailedLoginAttempts());
    }

    @Test
    void shouldResetFailedLoginAttempts() {
        AuthUser authUser =
                new AuthUser("user@example.com", "hashed-password");

        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();

        authUser.resetFailedLoginAttempts();

        assertEquals(0, authUser.getFailedLoginAttempts());
    }

    @Test
    void shouldLockUserAccount() {
        AuthUser authUser =
                new AuthUser("user@example.com", "hashed-password");

        authUser.lock();

        assertEquals(UserStatus.LOCKED, authUser.getStatus());
    }
}
