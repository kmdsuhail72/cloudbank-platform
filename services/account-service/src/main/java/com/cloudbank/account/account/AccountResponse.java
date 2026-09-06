package com.cloudbank.account.account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(

        UUID id,

        String accountNumber,

        AccountType accountType,

        String currency,

        AccountStatus status,

        Instant createdAt,

        Instant updatedAt

) {

    public static AccountResponse from(
            Account account
    ) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
