package com.cloudbank.transaction.transaction;

import jakarta.validation.constraints.NotNull;

public record TransactionStatusUpdateRequest(
        @NotNull TransactionStatus status
) {
}
