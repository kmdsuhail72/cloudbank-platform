package com.cloudbank.ledger.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublishingService {

    public enum PublishOutcome {
        NONE,
        PUBLISHED,
        FAILED
    }

    private final OutboxEventRepository repository;

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final String transferTopic;

    private final long sendTimeoutSeconds;

    public OutboxPublishingService(
            OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value(
                    "${cloudbank.kafka.topics.transfer}"
            )
            String transferTopic,
            @Value(
                    "${cloudbank.outbox.publisher.send-timeout-seconds}"
            )
            long sendTimeoutSeconds
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository
                );

        this.kafkaTemplate =
                Objects.requireNonNull(
                        kafkaTemplate
                );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper
                );

        this.transferTopic =
                requireText(
                        transferTopic,
                        "Transfer topic"
                );

        if (sendTimeoutSeconds < 1) {
            throw new IllegalArgumentException(
                    "Send timeout must be positive"
            );
        }

        this.sendTimeoutSeconds =
                sendTimeoutSeconds;
    }

    @Transactional
    public PublishOutcome publishNextPending() {
        Optional<OutboxEvent> pending =
                repository.claimNextPending();

        if (pending.isEmpty()) {
            return PublishOutcome.NONE;
        }

        OutboxEvent event =
                pending.get();

        try {
            String topic =
                    topicFor(
                            event
                    );

            JsonNode eventData =
                    objectMapper.readTree(
                            event.getPayload()
                    );

            OutboxKafkaEnvelope envelope =
                    new OutboxKafkaEnvelope(
                            event.getId(),
                            event.getEventType(),
                            event.getEventVersion(),
                            event.getAggregateType(),
                            event.getAggregateId(),
                            event.getCreatedAt(),
                            eventData
                    );

            String message =
                    objectMapper
                            .writeValueAsString(
                                    envelope
                            );

            /*
             * Do not mark the database row published until the
             * broker acknowledgement has completed successfully.
             */
            kafkaTemplate
                    .send(
                            topic,
                            event
                                    .getAggregateId()
                                    .toString(),
                            message
                    )
                    .get(
                            sendTimeoutSeconds,
                            TimeUnit.SECONDS
                    );

            event.markPublished(
                    Instant.now()
            );

            repository.saveAndFlush(
                    event
            );

            return PublishOutcome.PUBLISHED;

        } catch (Exception exception) {
            event.recordFailure(
                    errorMessage(
                            exception
                    )
            );

            /*
             * We intentionally swallow the broker exception after
             * persisting the failed-attempt state. Re-throwing here
             * would roll back attempt_count and last_error.
             */
            repository.saveAndFlush(
                    event
            );

            return PublishOutcome.FAILED;
        }
    }

    private String topicFor(
            OutboxEvent event
    ) {
        if (
                TransferOutboxService.AGGREGATE_TYPE.equals(
                        event.getAggregateType()
                )
                && TransferOutboxService.EVENT_TYPE.equals(
                        event.getEventType()
                )
        ) {
            return transferTopic;
        }

        throw new IllegalStateException(
                "Unsupported outbox event: "
                        + event.getAggregateType()
                        + "/"
                        + event.getEventType()
        );
    }

    private static String errorMessage(
            Throwable throwable
    ) {
        Throwable root =
                throwable;

        while (
                root.getCause() != null
                && root.getCause() != root
        ) {
            root =
                    root.getCause();
        }

        String message =
                root.getMessage();

        if (message == null
                || message.isBlank()) {
            return root
                    .getClass()
                    .getSimpleName();
        }

        return root
                .getClass()
                .getSimpleName()
                + ": "
                + message;
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
