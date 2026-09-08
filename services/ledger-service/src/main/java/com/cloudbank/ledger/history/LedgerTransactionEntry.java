package com.cloudbank.ledger.history;

import com.cloudbank.ledger.journal.LedgerEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerTransactionEntry(
        UUID postingId,
        UUID journalId,
        String referenceType,
        UUID referenceId,
        LedgerEntryType entryType,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
