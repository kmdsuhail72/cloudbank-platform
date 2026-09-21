package com.cloudbank.customer.profile;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
                        repository,
                        JsonMapper.builder().build()
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
    void shouldCreateProfileWithProvidedDetails() {
        UUID authUserId =
                UUID.randomUUID();

        LocalDate dateOfBirth =
                LocalDate.of(
                        1998,
                        1,
                        15
                );

        CustomerProfileCreateRequest request =
                new CustomerProfileCreateRequest(
                        "Suhail",
                        "Ahmed",
                        "+91-9999999999",
                        dateOfBirth
                );

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
                        authUserId,
                        request
                );

        assertEquals(
                authUserId,
                result.getAuthUserId()
        );

        assertEquals(
                "Suhail",
                result.getFirstName()
        );

        assertEquals(
                "Ahmed",
                result.getLastName()
        );

        assertEquals(
                "+91-9999999999",
                result.getPhoneNumber()
        );

        assertEquals(
                dateOfBirth,
                result.getDateOfBirth()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                result.getStatus()
        );
    }

    @Test
    void shouldReplaceExistingProfileDetails() {
        UUID authUserId =
                UUID.randomUUID();

        CustomerProfile existing =
                new CustomerProfile(
                        authUserId,
                        "Old",
                        "Name",
                        "+91-1111111111",
                        LocalDate.of(
                                1990,
                                1,
                                1
                        )
                );

        CustomerProfileUpdateRequest request =
                new CustomerProfileUpdateRequest(
                        "New",
                        "Customer",
                        "+91-2222222222",
                        LocalDate.of(
                                1998,
                                1,
                                15
                        )
                );

        when(
                repository.findByAuthUserId(
                        authUserId
                )
        ).thenReturn(
                Optional.of(
                        existing
                )
        );

        when(
                repository.saveAndFlush(
                        existing
                )
        ).thenReturn(
                existing
        );

        CustomerProfile updated =
                service.update(
                        authUserId,
                        request
                ).orElseThrow();

        assertEquals(
                "New",
                updated.getFirstName()
        );

        assertEquals(
                "Customer",
                updated.getLastName()
        );

        assertEquals(
                "+91-2222222222",
                updated.getPhoneNumber()
        );

        assertEquals(
                LocalDate.of(
                        1998,
                        1,
                        15
                ),
                updated.getDateOfBirth()
        );

        assertEquals(
                authUserId,
                updated.getAuthUserId()
        );
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingProfile() {
        UUID authUserId =
                UUID.randomUUID();

        CustomerProfileUpdateRequest request =
                new CustomerProfileUpdateRequest(
                        "New",
                        "Customer",
                        null,
                        null
                );

        when(
                repository.findByAuthUserId(
                        authUserId
                )
        ).thenReturn(
                Optional.empty()
        );

        Optional<CustomerProfile> result =
                service.update(
                        authUserId,
                        request
                );

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void shouldClearNullableProfileDetailsDuringReplacement() {
        UUID authUserId =
                UUID.randomUUID();

        CustomerProfile existing =
                new CustomerProfile(
                        authUserId,
                        "Old",
                        "Customer",
                        "+91-1111111111",
                        LocalDate.of(
                                1990,
                                1,
                                1
                        )
                );

        CustomerProfileUpdateRequest request =
                new CustomerProfileUpdateRequest(
                        null,
                        null,
                        null,
                        null
                );

        when(
                repository.findByAuthUserId(
                        authUserId
                )
        ).thenReturn(
                Optional.of(
                        existing
                )
        );

        when(
                repository.saveAndFlush(
                        existing
                )
        ).thenReturn(
                existing
        );

        CustomerProfile updated =
                service.update(
                        authUserId,
                        request
                ).orElseThrow();

        assertNull(
                updated.getFirstName()
        );

        assertNull(
                updated.getLastName()
        );

        assertNull(
                updated.getPhoneNumber()
        );

        assertNull(
                updated.getDateOfBirth()
        );

        assertEquals(
                authUserId,
                updated.getAuthUserId()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                updated.getStatus()
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
