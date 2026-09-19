package com.cloudbank.audit.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected AuditEvent() {
    }

    public AuditEvent(
            UUID eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            UUID aggregateId,
            Instant occurredAt,
            String payload
    ) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }
}
