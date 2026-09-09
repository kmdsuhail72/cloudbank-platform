package com.cloudbank.notification.transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferPostedData(
        UUID requestId,
        UUID journalId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        Instant postedAt
) {
}
