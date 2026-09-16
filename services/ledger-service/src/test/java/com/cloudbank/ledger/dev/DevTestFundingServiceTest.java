package com.cloudbank.ledger.dev;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerBalanceResult;
import com.cloudbank.ledger.journal.LedgerBalanceService;
import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournalService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DevTestFundingServiceTest {

    private final AccountOwnershipClient ownershipClient =
            mock(AccountOwnershipClient.class);

    private final LedgerJournalService journalService =
            mock(LedgerJournalService.class);

    private final LedgerBalanceService balanceService =
            mock(LedgerBalanceService.class);

    private final DevTestFundingService service =
            new DevTestFundingService(
                    ownershipClient,
                    journalService,
                    balanceService
            );

    @Test
    void shouldPostBalancedFundingJournalForOwnedActiveAccount() {
        UUID requestId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(
                ownershipClient.requireOwnedAccount(
                        "jwt-token",
                        accountId
                )
        ).thenReturn(
                new AccountOwnershipResponse(
                        accountId,
                        "usd",
                        "ACTIVE"
                )
        );

        when(
                balanceService.getPostedBalance(
                        accountId,
                        "USD"
                )
        ).thenReturn(
                new LedgerBalanceResult(
                        accountId,
                        "USD",
                        new BigDecimal("100.0000")
                )
        );

        DevTestFundingResult result =
                service.fund(
                        "jwt-token",
                        new DevTestFundingRequest(
                                requestId,
                                accountId,
                                new BigDecimal("100.0000")
                        )
                );

        verify(journalService).postJournal(
                eq("DEV_TEST_FUNDING"),
                eq(requestId),
                ArgumentMatchers.argThat(postings ->
                        postings.size() == 2
                                && postings.get(0).accountId()
                                .equals(
                                        DevTestFundingService
                                                .DEV_TEST_CLEARING_ACCOUNT_ID
                                )
                                && postings.get(0).entryType()
                                == LedgerEntryType.DEBIT
                                && postings.get(0).amount()
                                .compareTo(
                                        new BigDecimal("100.0000")
                                ) == 0
                                && postings.get(0).currency()
                                .equals("USD")
                                && postings.get(1).accountId()
                                .equals(accountId)
                                && postings.get(1).entryType()
                                == LedgerEntryType.CREDIT
                                && postings.get(1).amount()
                                .compareTo(
                                        new BigDecimal("100.0000")
                                ) == 0
                                && postings.get(1).currency()
                                .equals("USD")
                )
        );

        assertEquals(
                requestId,
                result.requestId()
        );

        assertEquals(
                accountId,
                result.accountId()
        );

        assertEquals(
                new BigDecimal("100.0000"),
                result.postedBalance()
        );
    }

    @Test
    void shouldRejectInactiveAccountBeforeJournalWrite() {
        UUID accountId = UUID.randomUUID();

        when(
                ownershipClient.requireOwnedAccount(
                        "jwt-token",
                        accountId
                )
        ).thenReturn(
                new AccountOwnershipResponse(
                        accountId,
                        "USD",
                        "FROZEN"
                )
        );

        assertThrows(
                DevTestFundingException.class,
                () -> service.fund(
                        "jwt-token",
                        new DevTestFundingRequest(
                                UUID.randomUUID(),
                                accountId,
                                new BigDecimal("10.0000")
                        )
                )
        );

        verifyNoInteractions(
                journalService,
                balanceService
        );
    }

    @Test
    void shouldRejectMoreThanFourDecimalPlaces() {
        assertThrows(
                DevTestFundingException.class,
                () -> service.fund(
                        "jwt-token",
                        new DevTestFundingRequest(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                new BigDecimal("1.00001")
                        )
                )
        );

        verifyNoInteractions(
                ownershipClient,
                journalService,
                balanceService
        );
    }
}
