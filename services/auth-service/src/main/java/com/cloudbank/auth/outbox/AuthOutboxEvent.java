package com.cloudbank.auth.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "auth_outbox_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_outbox_aggregate_event",
                        columnNames = {
                                "aggregate_type",
                                "aggregate_id",
                                "event_type"
                        }
                )
        }
)
public class AuthOutboxEvent {

    @Id
    @Column(
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 40,
            updatable = false
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false,
            updatable = false
    )
    private UUID aggregateId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 80,
            updatable = false
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false,
            updatable = false
    )
    private int eventVersion;

    @Column(
            nullable = false,
            columnDefinition = "text",
            updatable = false
    )
    private String payload;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(
            name = "last_error",
            columnDefinition = "text"
    )
    private String lastError;

    protected AuthOutboxEvent() {
    }

    public AuthOutboxEvent(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            int eventVersion,
            String payload
    ) {
        this.id =
                UUID.randomUUID();

        this.aggregateType =
                requireText(
                        aggregateType,
                        "Aggregate type"
                );

        this.aggregateId =
                Objects.requireNonNull(
                        aggregateId,
                        "Aggregate ID is required"
                );

        this.eventType =
                requireText(
                        eventType,
                        "Event type"
                );

        if (eventVersion < 1) {
            throw new IllegalArgumentException(
                    "Event version must be positive"
            );
        }

        this.eventVersion =
                eventVersion;

        this.payload =
                requireText(
                        payload,
                        "Payload"
                );

        this.createdAt =
                Instant.now();

        this.attemptCount =
                0;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public void markPublished(
            Instant publishedAt
    ) {
        if (this.publishedAt != null) {
            throw new IllegalStateException(
                    "Outbox event is already published"
            );
        }

        this.publishedAt =
                Objects.requireNonNull(
                        publishedAt,
                        "Published time is required"
                );

        this.lastError =
                null;
    }

    public void recordFailure(
            String error
    ) {
        if (publishedAt != null) {
            throw new IllegalStateException(
                    "Published outbox event cannot fail"
            );
        }

        if (attemptCount < Integer.MAX_VALUE) {
            attemptCount++;
        }

        if (error == null
                || error.isBlank()) {
            lastError =
                    "Unknown publishing failure";
            return;
        }

        String normalized =
                error.strip();

        lastError =
                normalized.length() <= 2000
                        ? normalized
                        : normalized.substring(
                                0,
                                2000
                        );
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }

        return value;
    }
}
