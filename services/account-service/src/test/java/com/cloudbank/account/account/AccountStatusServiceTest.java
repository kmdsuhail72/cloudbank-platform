package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountStatusServiceTest {

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

    private AccountStatusService service;

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
                new AccountStatusService(
                        repository,
                        customerProfileClient
                );
    }

    @Test
    void shouldFreezeOwnedAccount() {

        Account account =
                account();

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
                repository.findForUpdateByIdAndCustomerId(
                        ACCOUNT_ID,
                        CUSTOMER_ID
                )
        ).thenReturn(
                Optional.of(
                        account
                )
        );

        when(
                repository.saveAndFlush(
                        account
                )
        ).thenReturn(
                account
        );

        Account result =
                service.updateCurrentCustomerAccountStatus(
                        "valid-token",
                        ACCOUNT_ID,
                        AccountStatus.FROZEN
                );

        assertSame(
                account,
                result
        );

        assertEquals(
                AccountStatus.FROZEN,
                result.getStatus()
        );

        verify(
                repository
        ).findForUpdateByIdAndCustomerId(
                ACCOUNT_ID,
                CUSTOMER_ID
        );

        verify(
                repository
        ).saveAndFlush(
                account
        );
    }

    @Test
    void shouldRejectStatusChangeWithoutCustomerProfile() {

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
                        service.updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.FROZEN
                        )
        );

        verifyNoInteractions(
                repository
        );
    }

    @Test
    void shouldHideUnownedAccountAsNotFound() {

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
                repository.findForUpdateByIdAndCustomerId(
                        ACCOUNT_ID,
                        CUSTOMER_ID
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AccountNotFoundException.class,
                () ->
                        service.updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.FROZEN
                        )
        );

        verify(
                repository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void shouldRejectCustomerRequestToCloseAccount() {

        Account account =
                account();

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
                repository.findForUpdateByIdAndCustomerId(
                        ACCOUNT_ID,
                        CUSTOMER_ID
                )
        ).thenReturn(
                Optional.of(
                        account
                )
        );

        assertThrows(
                AccountStatusChangeNotAllowedException.class,
                () ->
                        service.updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.CLOSED
                        )
        );

        verify(
                repository,
                never()
        ).saveAndFlush(
                any()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.getStatus()
        );
    }

    private Account account() {
        return new Account(
                CUSTOMER_ID,
                "CB11111111111111111111111111111111",
                AccountType.SAVINGS,
                "INR"
        );
    }
}
