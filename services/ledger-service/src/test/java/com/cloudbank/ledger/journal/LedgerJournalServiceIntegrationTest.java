package com.cloudbank.ledger.journal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LedgerJournalServiceIntegrationTest {

    @Autowired
    private LedgerJournalService service;

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @AfterEach
    void cleanDatabase() {
        postingRepository.deleteAll();
        postingRepository.flush();

        journalRepository.deleteAll();
        journalRepository.flush();
    }

    @Test
    void shouldCommitBalancedJournalAtomically() {
        UUID referenceId =
                UUID.randomUUID();

        LedgerJournalResult result =
                service.postJournal(
                        "TRANSFER",
                        referenceId,
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "250.0000"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "250.0000"
                                )
                        )
                );

        assertEquals(
                2,
                result.postingCount()
        );

        assertEquals(
                1,
                journalRepository.count()
        );

        assertEquals(
                2,
                postingRepository.count()
        );
    }

    @Test
    void shouldPersistNothingForUnbalancedJournal() {
        assertThrows(
                UnbalancedJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        UUID.randomUUID(),
                        List.of(
                                command(
                                        LedgerEntryType.DEBIT,
                                        "100.0000"
                                ),
                                command(
                                        LedgerEntryType.CREDIT,
                                        "99.0000"
                                )
                        )
                )
        );

        assertEquals(
                0,
                journalRepository.count()
        );

        assertEquals(
                0,
                postingRepository.count()
        );
    }

    @Test
    void shouldRejectDuplicateReferenceWithoutSecondJournal() {
        UUID referenceId =
                UUID.randomUUID();

        List<LedgerPostingCommand> postings =
                List.of(
                        command(
                                LedgerEntryType.DEBIT,
                                "75.0000"
                        ),
                        command(
                                LedgerEntryType.CREDIT,
                                "75.0000"
                        )
                );

        service.postJournal(
                "TRANSFER",
                referenceId,
                postings
        );

        assertThrows(
                DuplicateLedgerJournalException.class,
                () -> service.postJournal(
                        "TRANSFER",
                        referenceId,
                        postings
                )
        );

        assertEquals(
                1,
                journalRepository.count()
        );

        assertEquals(
                2,
                postingRepository.count()
        );
    }

    private static LedgerPostingCommand command(
            LedgerEntryType entryType,
            String amount
    ) {
        return new LedgerPostingCommand(
                UUID.randomUUID(),
                entryType,
                new BigDecimal(
                        amount
                ),
                "INR"
        );
    }
}
