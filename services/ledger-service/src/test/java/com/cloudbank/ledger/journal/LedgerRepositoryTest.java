package com.cloudbank.ledger.journal;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class LedgerRepositoryTest {

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @Test
    void shouldPersistJournalAndPostings() {
        UUID referenceId =
                UUID.randomUUID();

        LedgerJournal journal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TRANSFER",
                                referenceId
                        )
                );

        UUID debitAccountId =
                UUID.randomUUID();

        UUID creditAccountId =
                UUID.randomUUID();

        postingRepository.saveAllAndFlush(
                List.of(
                        new LedgerPosting(
                                journal.getId(),
                                debitAccountId,
                                LedgerEntryType.DEBIT,
                                new BigDecimal(
                                        "50.0000"
                                ),
                                "INR"
                        ),
                        new LedgerPosting(
                                journal.getId(),
                                creditAccountId,
                                LedgerEntryType.CREDIT,
                                new BigDecimal(
                                        "50.0000"
                                ),
                                "INR"
                        )
                )
        );

        assertTrue(
                journalRepository
                        .findByReferenceTypeAndReferenceId(
                                "TRANSFER",
                                referenceId
                        )
                        .isPresent()
        );

        assertEquals(
                2,
                postingRepository
                        .findByJournalIdOrderByCreatedAtAsc(
                                journal.getId()
                        )
                        .size()
        );

        assertEquals(
                1,
                postingRepository
                        .findByAccountIdOrderByCreatedAtAsc(
                                debitAccountId
                        )
                        .size()
        );
    }

    @Test
    void shouldCalculatePostedBalanceFromCreditsAndDebits() {
        UUID accountId =
                UUID.randomUUID();

        UUID counterpartyId =
                UUID.randomUUID();

        LedgerJournal creditJournal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TEST_CREDIT",
                                UUID.randomUUID()
                        )
                );

        postingRepository.saveAllAndFlush(
                List.of(
                        new LedgerPosting(
                                creditJournal.getId(),
                                accountId,
                                LedgerEntryType.CREDIT,
                                new BigDecimal(
                                        "100.0000"
                                ),
                                "INR"
                        ),
                        new LedgerPosting(
                                creditJournal.getId(),
                                counterpartyId,
                                LedgerEntryType.DEBIT,
                                new BigDecimal(
                                        "100.0000"
                                ),
                                "INR"
                        )
                )
        );

        LedgerJournal debitJournal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TEST_DEBIT",
                                UUID.randomUUID()
                        )
                );

        postingRepository.saveAllAndFlush(
                List.of(
                        new LedgerPosting(
                                debitJournal.getId(),
                                accountId,
                                LedgerEntryType.DEBIT,
                                new BigDecimal(
                                        "40.0000"
                                ),
                                "INR"
                        ),
                        new LedgerPosting(
                                debitJournal.getId(),
                                counterpartyId,
                                LedgerEntryType.CREDIT,
                                new BigDecimal(
                                        "40.0000"
                                ),
                                "INR"
                        )
                )
        );

        assertEquals(
                new BigDecimal(
                        "60.0000"
                ),
                postingRepository
                        .calculatePostedBalance(
                                accountId,
                                "INR"
                        )
        );
    }

}
