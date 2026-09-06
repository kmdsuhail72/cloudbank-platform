package com.cloudbank.account.account;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountStatusTransitionTest {

    @Test
    void shouldFreezeActiveAccount() {

        Account account =
                account();

        account.changeCustomerManagedStatus(
                AccountStatus.FROZEN
        );

        assertEquals(
                AccountStatus.FROZEN,
                account.getStatus()
        );
    }

    @Test
    void shouldReactivateFrozenAccount() {

        Account account =
                account();

        account.changeCustomerManagedStatus(
                AccountStatus.FROZEN
        );

        account.changeCustomerManagedStatus(
                AccountStatus.ACTIVE
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.getStatus()
        );
    }

    @Test
    void shouldAllowIdempotentActiveStatusRequest() {

        Account account =
                account();

        account.changeCustomerManagedStatus(
                AccountStatus.ACTIVE
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.getStatus()
        );
    }

    @Test
    void shouldRejectCustomerManagedClosedStatus() {

        Account account =
                account();

        assertThrows(
                AccountStatusChangeNotAllowedException.class,
                () ->
                        account.changeCustomerManagedStatus(
                                AccountStatus.CLOSED
                        )
        );

        assertEquals(
                AccountStatus.ACTIVE,
                account.getStatus()
        );
    }

    @Test
    void shouldRejectNullTargetStatus() {

        Account account =
                account();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        account.changeCustomerManagedStatus(
                                null
                        )
        );
    }

    private Account account() {
        return new Account(
                UUID.fromString(
                        "22222222-2222-2222-2222-222222222222"
                ),
                "CB11111111111111111111111111111111",
                AccountType.SAVINGS,
                "INR"
        );
    }
}
