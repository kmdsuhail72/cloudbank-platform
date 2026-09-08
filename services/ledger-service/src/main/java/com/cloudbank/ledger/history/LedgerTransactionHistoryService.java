package com.cloudbank.ledger.history;

import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LedgerTransactionHistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final LedgerPostingRepository postingRepository;

    public LedgerTransactionHistoryService(
            LedgerPostingRepository postingRepository
    ) {
        this.postingRepository =
                Objects.requireNonNull(
                        postingRepository
                );
    }

    public LedgerTransactionPage getTransactions(
            UUID accountId,
            int page,
            int size
    ) {
        Objects.requireNonNull(
                accountId,
                "Account ID is required"
        );

        validatePagination(
                page,
                size
        );

        Page<LedgerTransactionView> result =
                postingRepository
                        .findTransactionHistory(
                                accountId,
                                PageRequest.of(
                                        page,
                                        size
                                )
                        );

        List<LedgerTransactionEntry> transactions =
                result.getContent()
                        .stream()
                        .map(
                                LedgerTransactionHistoryService::toEntry
                        )
                        .toList();

        return new LedgerTransactionPage(
                accountId,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                transactions
        );
    }

    public LedgerTransactionEntry getTransaction(
            UUID accountId,
            UUID postingId
    ) {
        Objects.requireNonNull(
                accountId,
                "Account ID is required"
        );

        Objects.requireNonNull(
                postingId,
                "Posting ID is required"
        );

        return postingRepository
                .findTransactionDetail(
                        accountId,
                        postingId
                )
                .map(
                        LedgerTransactionHistoryService::toEntry
                )
                .orElseThrow(
                        TransactionNotFoundException::new
                );
    }

    private static LedgerTransactionEntry toEntry(
            LedgerTransactionView transaction
    ) {
        return new LedgerTransactionEntry(
                transaction.getPostingId(),
                transaction.getJournalId(),
                transaction.getReferenceType(),
                transaction.getReferenceId(),
                LedgerEntryType.valueOf(
                        transaction.getEntryType()
                ),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getCreatedAt()
        );
    }

    private static void validatePagination(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new InvalidTransactionHistoryRequestException(
                    "Page must be zero or greater"
            );
        }

        if (size < 1
                || size > MAX_PAGE_SIZE) {
            throw new InvalidTransactionHistoryRequestException(
                    "Size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }
}
