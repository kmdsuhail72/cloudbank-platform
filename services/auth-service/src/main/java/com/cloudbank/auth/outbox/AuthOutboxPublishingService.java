package com.cloudbank.auth.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthOutboxPublishingService {

    public enum PublishOutcome {
        NONE,
        PUBLISHED,
        FAILED
    }

    private final AuthOutboxEventRepository repository;

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final String userContactTopic;

    private final long sendTimeoutSeconds;

    public AuthOutboxPublishingService(
            AuthOutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value(
                    "${cloudbank.kafka.topics.user-contact}"
            )
            String userContactTopic,
            @Value(
                    "${cloudbank.outbox.publisher.send-timeout-seconds}"
            )
            long sendTimeoutSeconds
    ) {
        this.repository =
                Objects.requireNonNull(repository);

        this.kafkaTemplate =
                Objects.requireNonNull(kafkaTemplate);

        this.objectMapper =
                Objects.requireNonNull(objectMapper);

        this.userContactTopic =
                requireText(
                        userContactTopic,
                        "User contact topic"
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
        Optional<AuthOutboxEvent> pending =
                repository.claimNextPending();

        if (pending.isEmpty()) {
            return PublishOutcome.NONE;
        }

        AuthOutboxEvent event =
                pending.get();

        try {
            if (!UserContactOutboxService.AGGREGATE_TYPE
                    .equals(event.getAggregateType())
                    || !UserContactOutboxService.EVENT_TYPE
                    .equals(event.getEventType())) {
                throw new IllegalStateException(
                        "Unsupported Auth outbox event"
                );
            }

            JsonNode data =
                    objectMapper.readTree(
                            event.getPayload()
                    );

            String message =
                    objectMapper.writeValueAsString(
                            new AuthOutboxKafkaEnvelope(
                                    event.getId(),
                                    event.getEventType(),
                                    event.getEventVersion(),
                                    event.getAggregateType(),
                                    event.getAggregateId(),
                                    event.getCreatedAt(),
                                    data
                            )
                    );

            kafkaTemplate
                    .send(
                            userContactTopic,
                            event.getAggregateId()
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

            repository.saveAndFlush(
                    event
            );

            return PublishOutcome.FAILED;
        }
    }

    private static String errorMessage(
            Throwable throwable
    ) {
        Throwable root =
                throwable;

        while (root.getCause() != null
                && root.getCause() != root) {
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
