package com.cloudbank.ledger.dev;

import java.math.BigDecimal;
import java.util.UUID;

public record DevTestFundingResult(
        UUID requestId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        BigDecimal postedBalance
) {
}
