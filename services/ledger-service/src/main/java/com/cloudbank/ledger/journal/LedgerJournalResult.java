package com.cloudbank.ledger.journal;

import java.time.Instant;
import java.util.UUID;

public record LedgerJournalResult(
        UUID journalId,
        String referenceType,
        UUID referenceId,
        Instant createdAt,
        int postingCount
) {
}
