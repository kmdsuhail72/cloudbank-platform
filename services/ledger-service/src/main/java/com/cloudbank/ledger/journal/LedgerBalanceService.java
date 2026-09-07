package com.cloudbank.ledger.journal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class LedgerBalanceService {

    private final LedgerPostingRepository postingRepository;

    public LedgerBalanceService(
            LedgerPostingRepository postingRepository
    ) {
        this.postingRepository =
                Objects.requireNonNull(
                        postingRepository
                );
    }

    @Transactional(
            readOnly = true
    )
    public LedgerBalanceResult getPostedBalance(
            UUID accountId,
            String currency
    ) {
        Objects.requireNonNull(
                accountId,
                "Account ID is required"
        );

        String normalizedCurrency =
                requireCurrency(
                        currency
                );

        BigDecimal postedBalance =
                postingRepository
                        .calculatePostedBalance(
                                accountId,
                                normalizedCurrency
                        );

        if (postedBalance == null) {
            postedBalance =
                    BigDecimal.ZERO;
        }

        postedBalance =
                postedBalance.setScale(
                        4
                );

        return new LedgerBalanceResult(
                accountId,
                normalizedCurrency,
                postedBalance
        );
    }

    private static String requireCurrency(
            String currency
    ) {
        if (currency == null
                || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalized =
                currency.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalized.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be 3 characters"
            );
        }

        return normalized;
    }
}
