package com.cloudbank.ledger.journal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_postings"
)
public class LedgerPosting {

    @Id
    @Column(
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "journal_id",
            nullable = false,
            updatable = false
    )
    private UUID journalId;

    @Column(
            name = "account_id",
            nullable = false,
            updatable = false
    )
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entry_type",
            nullable = false,
            length = 10,
            updatable = false
    )
    private LedgerEntryType entryType;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3,
            updatable = false
    )
    private String currency;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected LedgerPosting() {
    }

    public LedgerPosting(
            UUID journalId,
            UUID accountId,
            LedgerEntryType entryType,
            BigDecimal amount,
            String currency
    ) {
        this.id =
                UUID.randomUUID();

        this.journalId =
                Objects.requireNonNull(
                        journalId,
                        "Journal ID is required"
                );

        this.accountId =
                Objects.requireNonNull(
                        accountId,
                        "Account ID is required"
                );

        this.entryType =
                Objects.requireNonNull(
                        entryType,
                        "Ledger entry type is required"
                );

        this.amount =
                requireAmount(
                        amount
                );

        this.currency =
                requireCurrency(
                        currency
                );
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id =
                    UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt =
                    Instant.now()
                            .truncatedTo(
                                    ChronoUnit.MICROS
                            );
        }
    }

    private static BigDecimal requireAmount(
            BigDecimal amount
    ) {
        Objects.requireNonNull(
                amount,
                "Amount is required"
        );

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        BigDecimal normalized;

        try {
            normalized =
                    amount.setScale(
                            4,
                            RoundingMode.UNNECESSARY
                    );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Amount must not exceed 4 decimal places"
            );
        }

        if (normalized.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount exceeds supported precision"
            );
        }

        return normalized;
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

    public UUID getId() {
        return id;
    }

    public UUID getJournalId() {
        return journalId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
