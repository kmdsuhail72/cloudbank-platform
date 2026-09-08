package com.cloudbank.ledger.history;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface LedgerTransactionView {

    UUID getPostingId();

    UUID getJournalId();

    String getReferenceType();

    UUID getReferenceId();

    String getEntryType();

    BigDecimal getAmount();

    String getCurrency();

    Instant getCreatedAt();
}
