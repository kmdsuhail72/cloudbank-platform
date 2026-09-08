package com.cloudbank.ledger.history;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionHistoryControllerTest {

    @Mock
    private AccountOwnershipClient accountOwnershipClient;

    @Mock
    private LedgerTransactionHistoryService historyService;

    @Test
    void shouldVerifyOwnershipBeforeReturningHistory() {
        UUID accountId =
                UUID.randomUUID();

        Jwt jwt =
                mock(
                        Jwt.class
                );

        when(
                jwt.getTokenValue()
        ).thenReturn(
                "access-token"
        );

        AccountOwnershipResponse account =
                new AccountOwnershipResponse(
                        accountId,
                        "INR",
                        "FROZEN"
                );

        LedgerTransactionPage expected =
                new LedgerTransactionPage(
                        accountId,
                        0,
                        20,
                        0,
                        0,
                        false,
                        List.of()
                );

        when(
                accountOwnershipClient
                        .requireOwnedAccount(
                                "access-token",
                                accountId
                        )
        ).thenReturn(
                account
        );

        when(
                historyService
                        .getTransactions(
                                accountId,
                                0,
                                20
                        )
        ).thenReturn(
                expected
        );

        LedgerTransactionHistoryController controller =
                new LedgerTransactionHistoryController(
                        accountOwnershipClient,
                        historyService
                );

        ResponseEntity<LedgerTransactionPage> response =
                controller.getTransactions(
                        jwt,
                        accountId,
                        0,
                        20
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                expected,
                response.getBody()
        );

        verify(
                accountOwnershipClient
        ).requireOwnedAccount(
                "access-token",
                accountId
        );

        verify(
                historyService
        ).getTransactions(
                accountId,
                0,
                20
        );
    }
}
