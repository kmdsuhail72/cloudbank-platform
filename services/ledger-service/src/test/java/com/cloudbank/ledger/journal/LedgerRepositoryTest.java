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
}
