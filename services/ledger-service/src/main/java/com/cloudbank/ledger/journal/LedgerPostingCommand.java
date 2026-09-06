package com.cloudbank.ledger.journal;

import java.math.BigDecimal;
import java.util.UUID;

public record LedgerPostingCommand(
        UUID accountId,
        LedgerEntryType entryType,
        BigDecimal amount,
        String currency
) {
}
