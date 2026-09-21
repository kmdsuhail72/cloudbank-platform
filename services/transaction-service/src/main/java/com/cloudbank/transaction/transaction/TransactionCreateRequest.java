package com.cloudbank.transaction.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreateRequest(
        @NotNull UUID accountId,
        UUID counterpartyAccountId,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @Size(max = 255) String description,
        @NotNull TransactionType type,
        @NotNull TransactionStatus status
) {
}
