package com.cloudbank.ledger.history;

import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionHistoryServiceTest {

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
    void shouldMapPaginatedTransactionHistory() {
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

        PageRequest pageable =
                PageRequest.of(
                        1,
                        10
                );

        when(
                postingRepository
                        .findTransactionHistory(
                                accountId,
                                pageable
                        )
        ).thenReturn(
                new PageImpl<>(
                        List.of(view),
                        pageable,
                        25
                )
        );

        LedgerTransactionPage result =
                service.getTransactions(
                        accountId,
                        1,
                        10
                );

        assertEquals(
                accountId,
                result.accountId()
        );

        assertEquals(
                1,
                result.page()
        );

        assertEquals(
                10,
                result.size()
        );

        assertEquals(
                25,
                result.totalElements()
        );

        assertEquals(
                3,
                result.totalPages()
        );

        assertTrue(
                result.hasNext()
        );

        assertEquals(
                1,
                result.transactions().size()
        );

        LedgerTransactionEntry entry =
                result.transactions().get(
                        0
                );

        assertEquals(
                postingId,
                entry.postingId()
        );

        assertEquals(
                journalId,
                entry.journalId()
        );

        assertEquals(
                "TRANSFER",
                entry.referenceType()
        );

        assertEquals(
                referenceId,
                entry.referenceId()
        );

        assertEquals(
                LedgerEntryType.DEBIT,
                entry.entryType()
        );

        assertEquals(
                new BigDecimal(
                        "25.0000"
                ),
                entry.amount()
        );

        assertEquals(
                "INR",
                entry.currency()
        );

        assertEquals(
                createdAt,
                entry.createdAt()
        );

        verify(
                postingRepository
        ).findTransactionHistory(
                accountId,
                pageable
        );
    }

    @Test
    void shouldReturnEmptyPage() {
        UUID accountId =
                UUID.randomUUID();

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20
                );

        when(
                postingRepository
                        .findTransactionHistory(
                                accountId,
                                pageable
                        )
        ).thenReturn(
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                )
        );

        LedgerTransactionPage result =
                service.getTransactions(
                        accountId,
                        0,
                        20
                );

        assertTrue(
                result.transactions().isEmpty()
        );

        assertEquals(
                0,
                result.totalElements()
        );
    }

    @Test
    void shouldRejectNegativePage() {
        assertThrows(
                InvalidTransactionHistoryRequestException.class,
                () -> service.getTransactions(
                        UUID.randomUUID(),
                        -1,
                        20
                )
        );
    }

    @Test
    void shouldRejectInvalidPageSize() {
        assertThrows(
                InvalidTransactionHistoryRequestException.class,
                () -> service.getTransactions(
                        UUID.randomUUID(),
                        0,
                        0
                )
        );

        assertThrows(
                InvalidTransactionHistoryRequestException.class,
                () -> service.getTransactions(
                        UUID.randomUUID(),
                        0,
                        101
                )
        );
    }
}
