package com.cloudbank.customer.profile;

import java.time.Instant;
import java.util.UUID;

public record UserContactEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        UUID aggregateId,
        Instant occurredAt,
        UserContactData data
) {
}
