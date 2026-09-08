package com.cloudbank.ledger.history;

import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournal;
import com.cloudbank.ledger.journal.LedgerJournalRepository;
import com.cloudbank.ledger.journal.LedgerPosting;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class LedgerTransactionDetailRepositoryTest {

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @Test
    void shouldReturnJoinedDetailForMatchingAccount() {
        UUID accountId =
                UUID.randomUUID();

        UUID referenceId =
                UUID.randomUUID();

        LedgerJournal journal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TRANSFER",
                                referenceId
                        )
                );

        LedgerPosting posting =
                postingRepository.saveAndFlush(
                        new LedgerPosting(
                                journal.getId(),
                                accountId,
                                LedgerEntryType.CREDIT,
                                new BigDecimal(
                                        "12.5000"
                                ),
                                "INR"
                        )
                );

        Optional<LedgerTransactionView> result =
                postingRepository
                        .findTransactionDetail(
                                accountId,
                                posting.getId()
                        );

        assertTrue(
                result.isPresent()
        );

        LedgerTransactionView detail =
                result.orElseThrow();

        assertEquals(
                posting.getId(),
                detail.getPostingId()
        );

        assertEquals(
                journal.getId(),
                detail.getJournalId()
        );

        assertEquals(
                "TRANSFER",
                detail.getReferenceType()
        );

        assertEquals(
                referenceId,
                detail.getReferenceId()
        );

        assertEquals(
                "CREDIT",
                detail.getEntryType()
        );

        assertEquals(
                new BigDecimal(
                        "12.5000"
                ),
                detail.getAmount()
        );

        assertEquals(
                "INR",
                detail.getCurrency()
        );
    }

    @Test
    void shouldNotReturnPostingForDifferentAccount() {
        UUID actualAccountId =
                UUID.randomUUID();

        UUID otherAccountId =
                UUID.randomUUID();

        LedgerJournal journal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TRANSFER",
                                UUID.randomUUID()
                        )
                );

        LedgerPosting posting =
                postingRepository.saveAndFlush(
                        new LedgerPosting(
                                journal.getId(),
                                actualAccountId,
                                LedgerEntryType.DEBIT,
                                new BigDecimal(
                                        "5.0000"
                                ),
                                "INR"
                        )
                );

        Optional<LedgerTransactionView> result =
                postingRepository
                        .findTransactionDetail(
                                otherAccountId,
                                posting.getId()
                        );

        assertTrue(
                result.isEmpty()
        );
    }
}
