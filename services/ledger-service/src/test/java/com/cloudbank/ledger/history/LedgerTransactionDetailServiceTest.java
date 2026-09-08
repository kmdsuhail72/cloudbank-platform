package com.cloudbank.ledger.history;

import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionDetailServiceTest {

    @Mock
    private LedgerPostingRepository postingRepository;

    private LedgerTransactionHistoryService service;

    @BeforeEach
    void setUp() {
        service =
                new LedgerTransactionHistoryService(
                        postingRepository
                );
    }

    @Test
    void shouldReturnOwnedTransactionDetail() {
        UUID accountId =
                UUID.randomUUID();

        UUID postingId =
                UUID.randomUUID();

        UUID journalId =
                UUID.randomUUID();

        UUID referenceId =
                UUID.randomUUID();

        Instant createdAt =
                Instant.now();

        LedgerTransactionView view =
                mock(
                        LedgerTransactionView.class
                );

        when(view.getPostingId())
                .thenReturn(postingId);

        when(view.getJournalId())
                .thenReturn(journalId);

        when(view.getReferenceType())
                .thenReturn("TRANSFER");

        when(view.getReferenceId())
                .thenReturn(referenceId);

        when(view.getEntryType())
                .thenReturn("DEBIT");

        when(view.getAmount())
                .thenReturn(
                        new BigDecimal(
                                "25.0000"
                        )
                );

        when(view.getCurrency())
                .thenReturn("INR");

        when(view.getCreatedAt())
                .thenReturn(createdAt);

        when(
                postingRepository
                        .findTransactionDetail(
                                accountId,
                                postingId
                        )
        ).thenReturn(
                Optional.of(view)
        );

        LedgerTransactionEntry result =
                service.getTransaction(
                        accountId,
                        postingId
                );

        assertEquals(
                postingId,
                result.postingId()
        );

        assertEquals(
                journalId,
                result.journalId()
        );

        assertEquals(
                referenceId,
                result.referenceId()
        );

        assertEquals(
                LedgerEntryType.DEBIT,
                result.entryType()
        );

        assertEquals(
                new BigDecimal(
                        "25.0000"
                ),
                result.amount()
        );

        assertEquals(
                "INR",
                result.currency()
        );

        assertEquals(
                createdAt,
                result.createdAt()
        );

        verify(
                postingRepository
        ).findTransactionDetail(
                accountId,
                postingId
        );
    }

    @Test
    void shouldHideMissingOrOtherAccountTransaction() {
        UUID accountId =
                UUID.randomUUID();

        UUID postingId =
                UUID.randomUUID();

        when(
                postingRepository
                        .findTransactionDetail(
                                accountId,
                                postingId
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                TransactionNotFoundException.class,
                () -> service.getTransaction(
                        accountId,
                        postingId
                )
        );
    }
}
