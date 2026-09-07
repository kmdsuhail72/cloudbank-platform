package com.cloudbank.ledger.journal;

import java.math.BigDecimal;
import java.util.UUID;

public record LedgerBalanceResult(
        UUID accountId,
        String currency,
        BigDecimal postedBalance
) {
}
