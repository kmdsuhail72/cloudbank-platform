package com.cloudbank.ledger.history;

import java.util.List;
import java.util.UUID;

public record LedgerTransactionPage(
        UUID accountId,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        List<LedgerTransactionEntry> transactions
) {

    public LedgerTransactionPage {
        transactions =
                List.copyOf(
                        transactions
                );
    }
}
