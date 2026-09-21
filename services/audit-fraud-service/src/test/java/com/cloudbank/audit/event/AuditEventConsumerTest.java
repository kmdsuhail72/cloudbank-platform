package com.cloudbank.audit.event;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditEventConsumerTest {
    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);
    private final AuditEventConsumer consumer = new AuditEventConsumer(repository, JsonMapper.builder().build());
    private final UUID eventId = UUID.randomUUID();

    private String message() {
        return """
                {"eventId":"%s","eventType":"TRANSFER_POSTED","eventVersion":1,
                 "aggregateType":"TRANSFER","aggregateId":"%s",
                 "occurredAt":"2026-09-20T00:00:00Z","data":{"amount":10}}
                """.formatted(eventId, UUID.randomUUID());
    }

    @Test
    void persistsBeforeAcknowledging() {
        consumer.consume(message(), acknowledgment);
        var ordered = inOrder(repository, acknowledgment);
        ordered.verify(repository).existsByEventId(eventId);
        ordered.verify(repository).saveAndFlush(any(AuditEvent.class));
        ordered.verify(acknowledgment).acknowledge();
    }

    @Test
    void duplicateIsAcknowledgedWithoutAnotherInsert() {
        when(repository.existsByEventId(eventId)).thenReturn(true);
        consumer.consume(message(), acknowledgment);
        verify(repository, never()).saveAndFlush(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void failedPersistenceIsNotAcknowledged() {
        when(repository.saveAndFlush(any())).thenThrow(new IllegalStateException("database unavailable"));
        assertThrows(IllegalStateException.class, () -> consumer.consume(message(), acknowledgment));
        verifyNoInteractions(acknowledgment);
    }

    @Test
    void malformedOrIncompleteEventsAreNotAcknowledged() {
        assertThrows(IllegalArgumentException.class, () -> consumer.consume("not-json", acknowledgment));
        assertThrows(IllegalArgumentException.class, () -> consumer.consume("{}", acknowledgment));
        verifyNoInteractions(repository, acknowledgment);
    }
}
