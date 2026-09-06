package com.cloudbank.ledger.journal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LedgerPersistenceModelTest {

    @Test
    void shouldNormalizeJournalReferenceType() {
        LedgerJournal journal =
                new LedgerJournal(
                        "  TRANSFER  ",
                        UUID.randomUUID()
                );

        assertEquals(
                "TRANSFER",
                journal.getReferenceType()
        );
    }

    @Test
    void shouldNormalizePostingAmountAndCurrency() {
        LedgerPosting posting =
                new LedgerPosting(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerEntryType.DEBIT,
                        new BigDecimal(
                                "125.50"
                        ),
                        "inr"
                );

        assertEquals(
                new BigDecimal(
                        "125.5000"
                ),
                posting.getAmount()
        );

        assertEquals(
                "INR",
                posting.getCurrency()
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LedgerPosting(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerEntryType.DEBIT,
                        BigDecimal.ZERO,
                        "INR"
                )
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LedgerPosting(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerEntryType.CREDIT,
                        new BigDecimal(
                                "-1.0000"
                        ),
                        "INR"
                )
        );
    }

    @Test
    void shouldRejectMoreThanFourDecimalPlaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LedgerPosting(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerEntryType.DEBIT,
                        new BigDecimal(
                                "1.00001"
                        ),
                        "INR"
                )
        );
    }
}
