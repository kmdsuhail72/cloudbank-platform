package com.cloudbank.transaction.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID authUserId,
        UUID accountId,
        UUID counterpartyAccountId,
        BigDecimal amount,
        String currency,
        String description,
        TransactionType type,
        TransactionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAuthUserId(),
                transaction.getAccountId(),
                transaction.getCounterpartyAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
