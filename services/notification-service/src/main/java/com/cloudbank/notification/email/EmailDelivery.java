package com.cloudbank.notification.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "email_deliveries")
public class EmailDelivery {

    public enum Status {
        PENDING,
        PROCESSING,
        SENT
    }

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "notification_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID notificationId;

    @Column(
            name = "recipient_user_id",
            nullable = false,
            updatable = false
    )
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Status status;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(
            name = "next_attempt_at",
            nullable = false
    )
    private Instant nextAttemptAt;

    @Column(name = "claim_token")
    private UUID claimToken;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected EmailDelivery() {
    }

    public EmailDelivery(
            UUID id,
            UUID notificationId,
            UUID recipientUserId,
            Instant nextAttemptAt
    ) {
        this.id =
                Objects.requireNonNull(
                        id,
                        "Email delivery ID is required"
                );

        this.notificationId =
                Objects.requireNonNull(
                        notificationId,
                        "Notification ID is required"
                );

        this.recipientUserId =
                Objects.requireNonNull(
                        recipientUserId,
                        "Recipient user ID is required"
                );

        this.nextAttemptAt =
                Objects.requireNonNull(
                        nextAttemptAt,
                        "Next attempt time is required"
                );

        this.status =
                Status.PENDING;

        this.attemptCount =
                0;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now =
                Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public Status getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public UUID getClaimToken() {
        return claimToken;
    }

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
