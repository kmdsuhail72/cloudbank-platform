package com.cloudbank.audit.event;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record CloudEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        UUID aggregateId,
        Instant occurredAt,
        JsonNode data
) {
}
