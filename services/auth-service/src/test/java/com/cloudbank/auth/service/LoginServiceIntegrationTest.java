package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.InvalidCredentialsException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.model.UserStatus;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LoginServiceIntegrationTest {

    private static final String TEST_EMAIL =
            "login-rollback-test@cloudbank.local";

    @Autowired
    private LoginService loginService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanBeforeTest() {
        deleteTestUser();
    }

    @AfterEach
    void cleanAfterTest() {
        deleteTestUser();
    }

    @Test
    void shouldPersistFailedLoginAttemptAfterInvalidPassword() {
        AuthUser authUser = new AuthUser(
                TEST_EMAIL,
                passwordEncoder.encode("StrongPass123")
        );

        authUserRepository.save(authUser);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        TEST_EMAIL,
                        "WrongPass123"
                )
        );

        AuthUser reloadedUser = authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();

        assertEquals(
                1,
                reloadedUser.getFailedLoginAttempts()
        );
    }

    @Test
    void shouldPersistLockedStatusAfterFifthFailedLoginAttempt() {
        AuthUser authUser = new AuthUser(
                TEST_EMAIL,
                passwordEncoder.encode("StrongPass123")
        );

        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();
        authUser.recordFailedLoginAttempt();

        authUserRepository.save(authUser);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        TEST_EMAIL,
                        "WrongPass123"
                )
        );

        AuthUser reloadedUser = authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();

        assertEquals(
                5,
                reloadedUser.getFailedLoginAttempts()
        );

        assertEquals(
                UserStatus.LOCKED,
                reloadedUser.getStatus()
        );
    }

    private void deleteTestUser() {
        authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(authUserRepository::delete);
    }
}
