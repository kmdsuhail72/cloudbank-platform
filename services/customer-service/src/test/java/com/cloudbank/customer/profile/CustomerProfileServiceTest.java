package com.cloudbank.customer.profile;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerProfileServiceTest {

    private CustomerProfileRepository repository;

    private CustomerProfileService service;

    @BeforeEach
    void setUp() {
        repository =
                Mockito.mock(
                        CustomerProfileRepository.class
                );

        service =
                new CustomerProfileService(
                        repository
                );
    }

    @Test
    void shouldReturnProfileForAuthUserId() {
        UUID authUserId =
                UUID.randomUUID();

        CustomerProfile profile =
                new CustomerProfile(
                        authUserId
                );

        when(
                repository.findByAuthUserId(
                        authUserId
                )
        ).thenReturn(
                Optional.of(
                        profile
                )
        );

        Optional<CustomerProfile> result =
                service.findByAuthUserId(
                        authUserId
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                authUserId,
                result.orElseThrow()
                        .getAuthUserId()
        );

        verify(
                repository
        ).findByAuthUserId(
                authUserId
        );
    }

    @Test
    void shouldReturnEmptyWhenProfileDoesNotExist() {
        UUID authUserId =
                UUID.randomUUID();

        when(
                repository.findByAuthUserId(
                        authUserId
                )
        ).thenReturn(
                Optional.empty()
        );

        Optional<CustomerProfile> result =
                service.findByAuthUserId(
                        authUserId
                );

        assertTrue(
                result.isEmpty()
        );

        verify(
                repository
        ).findByAuthUserId(
                authUserId
        );
    }

    @Test
    void shouldCreateProfileForAuthUserId() {
        UUID authUserId =
                UUID.randomUUID();

        when(
                repository.saveAndFlush(
                        any(CustomerProfile.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        CustomerProfile result =
                service.create(
                        authUserId
                );

        assertEquals(
                authUserId,
                result.getAuthUserId()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                result.getStatus()
        );

        verify(
                repository
        ).saveAndFlush(
                any(CustomerProfile.class)
        );
    }

    @Test
    void shouldTranslateDuplicateAuthUserConstraint() {
        UUID authUserId =
                UUID.randomUUID();

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "duplicate customer profile",
                        new SQLException(
                                "duplicate customer profile",
                                "23505"
                        ),
                        "uk_customer_profiles_auth_user"
                );

        when(
                repository.saveAndFlush(
                        any(CustomerProfile.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate customer profile",
                        constraintViolation
                )
        );

        assertThrows(
                CustomerProfileAlreadyExistsException.class,
                () -> service.create(
                        authUserId
                )
        );
    }

    @Test
    void shouldRethrowUnrelatedDatabaseViolation() {
        UUID authUserId =
                UUID.randomUUID();

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "status constraint violation",
                        new SQLException(
                                "status constraint violation",
                                "23514"
                        ),
                        "chk_customer_profiles_status"
                );

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "status constraint violation",
                        constraintViolation
                );

        when(
                repository.saveAndFlush(
                        any(CustomerProfile.class)
                )
        ).thenThrow(
                databaseException
        );

        DataIntegrityViolationException thrown =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> service.create(
                                authUserId
                        )
                );

        assertSame(
                databaseException,
                thrown
        );
    }
}
