package com.cloudbank.notification.contact;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
public class RecipientContactProcessingService {

    public enum ProcessingOutcome {
        PROCESSED,
        DUPLICATE
    }

    private static final String EVENT_TYPE =
            "USER_CONTACT_REGISTERED";

    private static final String AGGREGATE_TYPE =
            "AUTH_USER";

    private static final int EVENT_VERSION =
            1;

    private final JdbcTemplate jdbcTemplate;

    private final RecipientContactRepository
            contactRepository;

    private final ObjectMapper objectMapper;

    public RecipientContactProcessingService(
            JdbcTemplate jdbcTemplate,
            RecipientContactRepository contactRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate
                );

        this.contactRepository =
                Objects.requireNonNull(
                        contactRepository
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
        UserContactEventEnvelope event =
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

        UserContactData data =
                event.data();

        contactRepository.saveAndFlush(
                new RecipientContact(
                        data.userId(),
                        data.email(),
                        event.eventId(),
                        event.occurredAt()
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

    private UserContactEventEnvelope deserialize(
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
                    UserContactEventEnvelope.class
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid user contact event envelope",
                    exception
            );
        }
    }

    private static void validate(
            UserContactEventEnvelope event
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

        if (event.eventVersion()
                != EVENT_VERSION) {
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

        UserContactData data =
                Objects.requireNonNull(
                        event.data(),
                        "User contact data is required"
                );

        Objects.requireNonNull(
                data.userId(),
                "User ID is required"
        );

        if (!event.aggregateId()
                .equals(data.userId())) {
            throw new IllegalArgumentException(
                    "Aggregate ID must match user ID"
            );
        }

        String email =
                data.email();

        if (email == null
                || email.isBlank()
                || email.length() > 320
                || !email.equals(email.strip())
                || !email.equals(
                        email.toLowerCase(
                                Locale.ROOT
                        )
                )) {
            throw new IllegalArgumentException(
                    "Normalized recipient email is required"
            );
        }
    }
}
