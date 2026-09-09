package com.cloudbank.ledger.outbox;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record OutboxKafkaEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        UUID aggregateId,
        Instant occurredAt,
        JsonNode data
) {
}
