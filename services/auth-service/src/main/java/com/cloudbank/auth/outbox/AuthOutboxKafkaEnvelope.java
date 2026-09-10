package com.cloudbank.auth.outbox;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record AuthOutboxKafkaEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        UUID aggregateId,
        Instant occurredAt,
        JsonNode data
) {
}
