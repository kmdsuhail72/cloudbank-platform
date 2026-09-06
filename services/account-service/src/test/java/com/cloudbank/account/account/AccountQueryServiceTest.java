package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountQueryServiceTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private AccountRepository repository;

    private CustomerProfileClient customerProfileClient;

    private AccountQueryService service;

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

        service =
                new AccountQueryService(
                        repository,
                        customerProfileClient
                );
    }

    @Test
    void shouldReturnAccountsOwnedByResolvedCustomer() {

        Account account =
                new Account(
                        CUSTOMER_ID,
                        "CB11111111111111111111111111111111",
                        AccountType.SAVINGS,
                        "INR"
                );

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
                repository.findByCustomerId(
                        CUSTOMER_ID
                )
        ).thenReturn(
                List.of(
                        account
                )
        );

        List<Account> accounts =
                service.findCurrentCustomerAccounts(
                        "valid-token"
                );

        assertEquals(
                List.of(
                        account
                ),
                accounts
        );

        verify(
                repository
        ).findByCustomerId(
                CUSTOMER_ID
        );
    }

    @Test
    void shouldRejectListWhenCustomerProfileIsMissing() {

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
                        service.findCurrentCustomerAccounts(
                                "valid-token"
                        )
        );

        verify(
                repository,
                never()
        ).findByCustomerId(
                CUSTOMER_ID
        );
    }

    @Test
    void shouldReturnAccountOnlyForResolvedCustomer() {

        Account account =
                new Account(
                        CUSTOMER_ID,
                        "CB22222222222222222222222222222222",
                        AccountType.CHECKING,
                        "USD"
                );

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
                repository.findByIdAndCustomerId(
                        ACCOUNT_ID,
                        CUSTOMER_ID
                )
        ).thenReturn(
                Optional.of(
                        account
                )
        );

        Account result =
                service.findCurrentCustomerAccount(
                        "valid-token",
                        ACCOUNT_ID
                );

        assertSame(
                account,
                result
        );

        verify(
                repository
        ).findByIdAndCustomerId(
                ACCOUNT_ID,
                CUSTOMER_ID
        );
    }

    @Test
    void shouldHideMissingOrUnownedAccountAsNotFound() {

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
                repository.findByIdAndCustomerId(
                        ACCOUNT_ID,
                        CUSTOMER_ID
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AccountNotFoundException.class,
                () ->
                        service.findCurrentCustomerAccount(
                                "valid-token",
                                ACCOUNT_ID
                        )
        );
    }
}
