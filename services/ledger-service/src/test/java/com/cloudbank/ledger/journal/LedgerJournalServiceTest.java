package com.cloudbank.ledger.journal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerJournalServiceTest {

    @Mock
    private LedgerJournalRepository journalRepository;

    @Mock
    private LedgerPostingRepository postingRepository;

    private LedgerJournalService service;

    @BeforeEach
    void setUp() {
        service =
                new LedgerJournalService(
                        journalRepository,
                        postingRepository
                );
    }

    @Test
    void shouldPersistBalancedJournal() {
        LedgerJournalResult result =
                service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "125.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "125.0000",
                                        "inr"
                                )
                        )
                );

        assertEquals(
                "TRANSFER",
                result.referenceType()
        );

        assertEquals(
                2,
                result.postingCount()
        );

        verify(
                journalRepository
        ).saveAndFlush(
                any(LedgerJournal.class)
        );

        verify(
                postingRepository
        ).saveAllAndFlush(
                anyList()
        );
    }

    @Test
    void shouldRejectUnbalancedJournalBeforePersistence() {
        assertThrows(
                UnbalancedJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "100.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "90.0000",
                                        "INR"
                                )
                        )
                )
        );

        verify(
                journalRepository,
                never()
        ).saveAndFlush(
                any()
        );

        verify(
                postingRepository,
                never()
        ).saveAllAndFlush(
                anyList()
        );
    }

    @Test
    void shouldRejectMixedCurrenciesBeforePersistence() {
        assertThrows(
                InvalidJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "50.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "50.0000",
                                        "USD"
                                )
                        )
                )
        );

        verify(
                journalRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void shouldRequireDebitAndCredit() {
        assertThrows(
                InvalidJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "25.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.DEBIT,
                                        "25.0000",
                                        "INR"
                                )
                        )
                )
        );

        verify(
                journalRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void shouldRequireAtLeastTwoPostings() {
        assertThrows(
                InvalidJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "10.0000",
                                        "INR"
                                )
                        )
                )
        );

        verify(
                journalRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void shouldTranslateDuplicateJournalReference() {
        when(
                journalRepository.saveAndFlush(
                        any(LedgerJournal.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate journal reference"
                )
        );

        assertThrows(
                DuplicateLedgerJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "75.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "75.0000",
                                        "INR"
                                )
                        )
                )
        );

        verify(
                postingRepository,
                never()
        ).saveAllAndFlush(
                anyList()
        );
    }

    @Test
    void shouldSupportMultiplePostingsWhenTotalsBalance() {
        LedgerJournalResult result =
                service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "100.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "60.0000",
                                        "INR"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "40.0000",
                                        "INR"
                                )
                        )
                );

        assertEquals(
                3,
                result.postingCount()
        );

        verify(
                postingRepository
        ).saveAllAndFlush(
                anyList()
        );
    }

    private static LedgerPostingCommand command(
            LedgerEntryType entryType,
            String amount,
            String currency
    ) {
        return new LedgerPostingCommand(
                UUID.randomUUID(),
                entryType,
                new BigDecimal(
                        amount
                ),
                currency
        );
    }
}
