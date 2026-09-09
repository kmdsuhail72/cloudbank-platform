package com.cloudbank.notification.transfer;

import java.time.Instant;
import java.util.UUID;

public record TransferEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        UUID aggregateId,
        Instant occurredAt,
        TransferPostedData data
) {
}
