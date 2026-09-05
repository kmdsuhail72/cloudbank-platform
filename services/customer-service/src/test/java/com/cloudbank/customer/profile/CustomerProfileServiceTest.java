package com.cloudbank.customer.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
