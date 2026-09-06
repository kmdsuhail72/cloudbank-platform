package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        Account savings =
                new Account(
                        CUSTOMER_ID,
                        "CB11111111111111111111111111111111",
                        AccountType.SAVINGS,
                        "INR"
                );

        Account checking =
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
                repository.findByCustomerId(
                        CUSTOMER_ID
                )
        ).thenReturn(
                List.of(
                        savings,
                        checking
                )
        );

        List<Account> accounts =
                service.findCurrentCustomerAccounts(
                        "valid-token"
                );

        assertEquals(
                List.of(
                        savings,
                        checking
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
    void shouldRejectQueryWhenCustomerProfileIsMissing() {

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
}
