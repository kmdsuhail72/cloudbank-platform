package com.cloudbank.ledger.dev;

import java.math.BigDecimal;
import java.util.UUID;

public record DevTestFundingRequest(
        UUID requestId,
        UUID accountId,
        BigDecimal amount
) {
}
