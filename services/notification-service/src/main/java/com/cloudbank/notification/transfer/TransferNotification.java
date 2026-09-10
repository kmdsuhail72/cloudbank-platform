package com.cloudbank.notification.transfer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transfer_notifications")
public class TransferNotification {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true
    )
    private UUID eventId;

    @Column(
            name = "transfer_request_id",
            nullable = false
    )
    private UUID transferRequestId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(
            name = "journal_id",
            nullable = false
    )
    private UUID journalId;

    @Column(
            name = "source_account_id",
            nullable = false
    )
    private UUID sourceAccountId;

    @Column(
            name = "destination_account_id",
            nullable = false
    )
    private UUID destinationAccountId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "posted_at",
            nullable = false
    )
    private Instant postedAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected TransferNotification() {
    }

    public TransferNotification(
            UUID id,
            UUID eventId,
            UUID transferRequestId,
            UUID journalId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            Instant postedAt
    ) {
        this(
                id,
                eventId,
                transferRequestId,
                null,
                journalId,
                sourceAccountId,
                destinationAccountId,
                amount,
                currency,
                postedAt
        );
    }

    public TransferNotification(
            UUID id,
            UUID eventId,
            UUID transferRequestId,
            UUID actorUserId,
            UUID journalId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            Instant postedAt
    ) {
        this.id =
                Objects.requireNonNull(id);

        this.eventId =
                Objects.requireNonNull(eventId);

        this.transferRequestId =
                Objects.requireNonNull(
                        transferRequestId
                );

        /*
         * Null is deliberately allowed for historical
         * TRANSFER_POSTED version-1 events.
         */
        this.actorUserId =
                actorUserId;

        this.journalId =
                Objects.requireNonNull(
                        journalId
                );

        this.sourceAccountId =
                Objects.requireNonNull(
                        sourceAccountId
                );

        this.destinationAccountId =
                Objects.requireNonNull(
                        destinationAccountId
                );

        this.amount =
                Objects.requireNonNull(amount);

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        if (currency == null
                || !currency.matches(
                        "^[A-Z]{3}$"
                )) {
            throw new IllegalArgumentException(
                    "Currency must be three uppercase letters"
            );
        }

        this.currency =
                currency;

        this.postedAt =
                Objects.requireNonNull(
                        postedAt
                );
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt =
                    Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getTransferRequestId() {
        return transferRequestId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public UUID getJournalId() {
        return journalId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
