package com.cloudbank.ledger.history;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerEntryType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionDetailControllerTest {

    @Mock
    private AccountOwnershipClient accountOwnershipClient;

    @Mock
    private LedgerTransactionHistoryService historyService;

    @Test
    void shouldVerifyOwnershipBeforeReturningDetail() {
        UUID accountId =
                UUID.randomUUID();

        UUID postingId =
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
                        "CLOSED"
                );

        LedgerTransactionEntry expected =
                new LedgerTransactionEntry(
                        postingId,
                        UUID.randomUUID(),
                        "TRANSFER",
                        UUID.randomUUID(),
                        LedgerEntryType.CREDIT,
                        new BigDecimal(
                                "25.0000"
                        ),
                        "INR",
                        Instant.now()
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
                        .getTransaction(
                                accountId,
                                postingId
                        )
        ).thenReturn(
                expected
        );

        LedgerTransactionHistoryController controller =
                new LedgerTransactionHistoryController(
                        accountOwnershipClient,
                        historyService
                );

        ResponseEntity<LedgerTransactionEntry> response =
                controller.getTransaction(
                        jwt,
                        accountId,
                        postingId
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
        ).getTransaction(
                accountId,
                postingId
        );
    }
}
