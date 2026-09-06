package com.cloudbank.ledger.journal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_journals"
)
public class LedgerJournal {

    @Id
    @Column(
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "reference_type",
            nullable = false,
            length = 40,
            updatable = false
    )
    private String referenceType;

    @Column(
            name = "reference_id",
            nullable = false,
            updatable = false
    )
    private UUID referenceId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected LedgerJournal() {
    }

    public LedgerJournal(
            String referenceType,
            UUID referenceId
    ) {
        this.id =
                UUID.randomUUID();

        this.referenceType =
                requireReferenceType(
                        referenceType
                );

        this.referenceId =
                Objects.requireNonNull(
                        referenceId,
                        "Reference ID is required"
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

    private static String requireReferenceType(
            String referenceType
    ) {
        if (referenceType == null
                || referenceType.isBlank()) {
            throw new IllegalArgumentException(
                    "Reference type is required"
            );
        }

        String normalized =
                referenceType.trim();

        if (normalized.length() > 40) {
            throw new IllegalArgumentException(
                    "Reference type must not exceed 40 characters"
            );
        }

        return normalized;
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
