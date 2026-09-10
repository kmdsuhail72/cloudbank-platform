package com.cloudbank.notification.transfer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransferNotificationProcessingService {

    public enum ProcessingOutcome {
        PROCESSED,
        DUPLICATE
    }

    private static final String EVENT_TYPE =
            "TRANSFER_POSTED";

    private static final String AGGREGATE_TYPE =
            "TRANSFER";

    private static final int LEGACY_EVENT_VERSION =
            1;

    private static final int CURRENT_EVENT_VERSION =
            2;

    private final JdbcTemplate jdbcTemplate;

    private final TransferNotificationRepository
            notificationRepository;

    private final ObjectMapper objectMapper;

    public TransferNotificationProcessingService(
            JdbcTemplate jdbcTemplate,
            TransferNotificationRepository notificationRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate
                );

        this.notificationRepository =
                Objects.requireNonNull(
                        notificationRepository
                );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper
                );
    }

    @Transactional
    public ProcessingOutcome process(
            String rawMessage
    ) {
        TransferEventEnvelope event =
                deserialize(
                        rawMessage
                );

        validate(
                event
        );

        int claimed =
                jdbcTemplate.update(
                        """
                        INSERT INTO inbox_events (
                            event_id,
                            event_type,
                            event_version,
                            aggregate_type,
                            aggregate_id,
                            occurred_at,
                            payload
                        ) VALUES (
                            ?, ?, ?, ?, ?, ?, ?
                        )
                        ON CONFLICT (event_id)
                        DO NOTHING
                        """,
                        event.eventId(),
                        event.eventType(),
                        event.eventVersion(),
                        event.aggregateType(),
                        event.aggregateId(),
                        Timestamp.from(
                                event.occurredAt()
                        ),
                        rawMessage
                );

        if (claimed == 0) {
            return ProcessingOutcome.DUPLICATE;
        }

        TransferPostedData data =
                event.data();

        notificationRepository.saveAndFlush(
                new TransferNotification(
                        UUID.randomUUID(),
                        event.eventId(),
                        data.requestId(),
                        data.actorUserId(),
                        data.journalId(),
                        data.sourceAccountId(),
                        data.destinationAccountId(),
                        data.amount(),
                        data.currency(),
                        data.postedAt()
                )
        );

        int processed =
                jdbcTemplate.update(
                        """
                        UPDATE inbox_events
                        SET processed_at = ?
                        WHERE event_id = ?
                          AND processed_at IS NULL
                        """,
                        Timestamp.from(
                                Instant.now()
                        ),
                        event.eventId()
                );

        if (processed != 1) {
            throw new IllegalStateException(
                    "Inbox event could not be marked processed"
            );
        }

        return ProcessingOutcome.PROCESSED;
    }

    private TransferEventEnvelope deserialize(
            String rawMessage
    ) {
        if (rawMessage == null
                || rawMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "Kafka message is required"
            );
        }

        try {
            return objectMapper.readValue(
                    rawMessage,
                    TransferEventEnvelope.class
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid transfer event envelope",
                    exception
            );
        }
    }

    private static void validate(
            TransferEventEnvelope event
    ) {
        Objects.requireNonNull(
                event,
                "Event is required"
        );

        Objects.requireNonNull(
                event.eventId(),
                "Event ID is required"
        );

        if (!EVENT_TYPE.equals(
                event.eventType()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported event type"
            );
        }

        int eventVersion =
                event.eventVersion();

        if (eventVersion != LEGACY_EVENT_VERSION
                && eventVersion != CURRENT_EVENT_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported event version"
            );
        }

        if (!AGGREGATE_TYPE.equals(
                event.aggregateType()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported aggregate type"
            );
        }

        Objects.requireNonNull(
                event.aggregateId(),
                "Aggregate ID is required"
        );

        Objects.requireNonNull(
                event.occurredAt(),
                "Occurred time is required"
        );

        TransferPostedData data =
                Objects.requireNonNull(
                        event.data(),
                        "Transfer data is required"
                );

        if (!event.aggregateId().equals(
                data.requestId()
        )) {
            throw new IllegalArgumentException(
                    "Aggregate ID must match request ID"
            );
        }

        if (eventVersion == CURRENT_EVENT_VERSION) {
            Objects.requireNonNull(
                    data.actorUserId(),
                    "Actor user ID is required for version 2"
            );
        }

        Objects.requireNonNull(
                data.journalId()
        );

        Objects.requireNonNull(
                data.sourceAccountId()
        );

        Objects.requireNonNull(
                data.destinationAccountId()
        );

        BigDecimal amount =
                Objects.requireNonNull(
                        data.amount()
                );

        if (amount.signum() <= 0
                || amount.scale() > 4
                || amount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Invalid transfer amount"
            );
        }

        if (data.currency() == null
                || !data.currency().matches(
                        "^[A-Z]{3}$"
                )) {
            throw new IllegalArgumentException(
                    "Invalid currency"
            );
        }

        Objects.requireNonNull(
                data.postedAt(),
                "Posted time is required"
        );
    }
}
