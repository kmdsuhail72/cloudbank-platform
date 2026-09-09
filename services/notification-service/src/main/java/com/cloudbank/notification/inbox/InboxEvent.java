package com.cloudbank.notification.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
public class InboxEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 80
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false
    )
    private int eventVersion;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 40
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private UUID aggregateId;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Column(
            name = "received_at",
            nullable = false
    )
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected InboxEvent() {
    }

    public InboxEvent(
            UUID eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            UUID aggregateId,
            Instant occurredAt,
            String payload
    ) {
        this.eventId =
                Objects.requireNonNull(eventId);

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

        this.aggregateType =
                requireText(
                        aggregateType,
                        "Aggregate type"
                );

        this.aggregateId =
                Objects.requireNonNull(
                        aggregateId
                );

        this.occurredAt =
                Objects.requireNonNull(
                        occurredAt
                );

        this.payload =
                requireText(
                        payload,
                        "Payload"
                );
    }

    @PrePersist
    void initializeReceivedAt() {
        if (receivedAt == null) {
            receivedAt =
                    Instant.now();
        }
    }

    public void markProcessed(
            Instant processedAt
    ) {
        if (this.processedAt != null) {
            throw new IllegalStateException(
                    "Inbox event is already processed"
            );
        }

        this.processedAt =
                Objects.requireNonNull(
                        processedAt
                );
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
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
