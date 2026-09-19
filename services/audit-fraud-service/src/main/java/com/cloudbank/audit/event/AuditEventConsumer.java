package com.cloudbank.audit.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(
        name = "cloudbank.audit.consumer.enabled",
        havingValue = "true"
)
public class AuditEventConsumer {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public AuditEventConsumer(
            AuditEventRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {
                    "${cloudbank.kafka.topics.user-contact}",
                    "${cloudbank.kafka.topics.transfer}"
            },
            groupId = "${cloudbank.audit.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment acknowledgment) {
        final CloudEventEnvelope event;
        try {
            event = objectMapper.readValue(message, CloudEventEnvelope.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid audit event envelope", exception);
        }

        if (event.eventId() == null
                || event.eventType() == null
                || event.aggregateType() == null
                || event.aggregateId() == null
                || event.occurredAt() == null
                || event.data() == null) {
            throw new IllegalArgumentException("Incomplete audit event envelope");
        }

        if (!repository.existsByEventId(event.eventId())) {
            repository.saveAndFlush(new AuditEvent(
                    event.eventId(),
                    event.eventType(),
                    event.eventVersion(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.occurredAt(),
                    message
            ));
        }

        acknowledgment.acknowledge();
    }
}
