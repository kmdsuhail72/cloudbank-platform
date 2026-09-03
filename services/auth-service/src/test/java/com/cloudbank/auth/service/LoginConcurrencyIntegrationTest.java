package com.cloudbank.auth.service;

import com.cloudbank.auth.exception.InvalidCredentialsException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class LoginConcurrencyIntegrationTest {

    private static final String TEST_EMAIL =
            "login-concurrency-test@cloudbank.local";

    private static final String STORED_PASSWORD_HASH =
            "stored-password-hash";

    @Autowired
    private LoginService loginService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void cleanBeforeTest() {
        deleteTestUser();
    }

    @AfterEach
    void cleanAfterTest() {
        deleteTestUser();
    }

    @Test
    void shouldPreserveBothConcurrentFailedLoginAttempts() throws Exception {
        AuthUser authUser = new AuthUser(
                TEST_EMAIL,
                STORED_PASSWORD_HASH
        );

        authUserRepository.saveAndFlush(authUser);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch passwordChecks = new CountDownLatch(2);

        when(passwordEncoder.matches(
                "WrongPass123",
                STORED_PASSWORD_HASH
        )).thenAnswer(invocation -> {
            passwordChecks.countDown();
            passwordChecks.await(1, TimeUnit.SECONDS);

            return false;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstAttempt = executor.submit(() -> {
                startGate.await();

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> loginService.login(
                                TEST_EMAIL,
                                "WrongPass123"
                        )
                );

                return null;
            });

            Future<?> secondAttempt = executor.submit(() -> {
                startGate.await();

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> loginService.login(
                                TEST_EMAIL,
                                "WrongPass123"
                        )
                );

                return null;
            });

            startGate.countDown();

            firstAttempt.get(5, TimeUnit.SECONDS);
            secondAttempt.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        AuthUser reloadedUser = authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();

        assertEquals(
                2,
                reloadedUser.getFailedLoginAttempts()
        );
    }

    private void deleteTestUser() {
        authUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(authUserRepository::delete);
    }
}
