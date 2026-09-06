package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountCreationServiceTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private AccountRepository repository;

    private CustomerProfileClient customerProfileClient;

    private AccountNumberGenerator accountNumberGenerator;

    private AccountCreationService service;

    @BeforeEach
    void setUp() {
        repository =
                mock(
                        AccountRepository.class
                );

        customerProfileClient =
                mock(
                        CustomerProfileClient.class
                );

        accountNumberGenerator =
                mock(
                        AccountNumberGenerator.class
                );

        service =
                new AccountCreationService(
                        repository,
                        customerProfileClient,
                        accountNumberGenerator
                );
    }

    @Test
    void shouldCreateAccountForResolvedCustomer() {

        when(
                customerProfileClient
                        .findCurrentCustomerId(
                                "valid-token"
                        )
        ).thenReturn(
                Optional.of(
                        CUSTOMER_ID
                )
        );

        when(
                accountNumberGenerator.generate()
        ).thenReturn(
                "CB1234567890ABCDEF1234567890ABCDEF"
        );

        when(
                repository.saveAndFlush(
                        any(
                                Account.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        Account account =
                service.create(
                        "valid-token",
                        AccountType.SAVINGS,
                        "inr"
                );

        assertEquals(
                CUSTOMER_ID,
                account.getCustomerId()
        );

        assertEquals(
                "CB1234567890ABCDEF1234567890ABCDEF",
                account.getAccountNumber()
        );

        assertEquals(
                AccountType.SAVINGS,
                account.getAccountType()
        );

        assertEquals(
                "INR",
                account.getCurrency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.getStatus()
        );

        verify(
                customerProfileClient
        ).findCurrentCustomerId(
                "valid-token"
        );

        verify(
                repository
        ).saveAndFlush(
                account
        );
    }

    @Test
    void shouldRejectCreationWhenCustomerProfileIsMissing() {

        when(
                customerProfileClient
                        .findCurrentCustomerId(
                                "valid-token"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                CustomerProfileRequiredException.class,
                () ->
                        service.create(
                                "valid-token",
                                AccountType.CHECKING,
                                "USD"
                        )
        );

        verify(
                accountNumberGenerator,
                never()
        ).generate();

        verify(
                repository,
                never()
        ).saveAndFlush(
                any(
                        Account.class
                )
        );
    }
}
