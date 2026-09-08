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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class LedgerTransactionHistoryRepositoryTest {

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @Test
    void shouldReturnJoinedAccountHistoryNewestFirst() {
        UUID accountId =
                UUID.randomUUID();

        LedgerJournal firstJournal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TRANSFER",
                                UUID.randomUUID()
                        )
                );

        LedgerPosting firstPosting =
                postingRepository.saveAndFlush(
                        new LedgerPosting(
                                firstJournal.getId(),
                                accountId,
                                LedgerEntryType.CREDIT,
                                new BigDecimal(
                                        "10.0000"
                                ),
                                "INR"
                        )
                );

        LedgerJournal secondJournal =
                journalRepository.saveAndFlush(
                        new LedgerJournal(
                                "TRANSFER",
                                UUID.randomUUID()
                        )
                );

        LedgerPosting secondPosting =
                postingRepository.saveAndFlush(
                        new LedgerPosting(
                                secondJournal.getId(),
                                accountId,
                                LedgerEntryType.DEBIT,
                                new BigDecimal(
                                        "4.0000"
                                ),
                                "INR"
                        )
                );

        Page<LedgerTransactionView> result =
                postingRepository
                        .findTransactionHistory(
                                accountId,
                                PageRequest.of(
                                        0,
                                        20
                                )
                        );

        assertEquals(
                2,
                result.getTotalElements()
        );

        List<UUID> actualIds =
                result.getContent()
                        .stream()
                        .map(
                                LedgerTransactionView::getPostingId
                        )
                        .toList();

        List<UUID> expectedIds =
                List.of(
                        firstPosting,
                        secondPosting
                )
                        .stream()
                        .sorted(
                                (left, right) -> {
                                    int created =
                                            right.getCreatedAt()
                                                    .compareTo(
                                                            left.getCreatedAt()
                                                    );

                                    if (created != 0) {
                                        return created;
                                    }

                                    return right.getId()
                                            .compareTo(
                                                    left.getId()
                                            );
                                }
                        )
                        .map(
                                LedgerPosting::getId
                        )
                        .toList();

        assertEquals(
                expectedIds,
                actualIds
        );

        assertEquals(
                "TRANSFER",
                result.getContent()
                        .get(0)
                        .getReferenceType()
        );
    }
}
