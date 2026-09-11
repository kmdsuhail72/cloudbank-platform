package com.cloudbank.notification.email;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "cloudbank.kafka.consumer.enabled=false",
                "cloudbank.kafka.contact-consumer.enabled=false",
                "cloudbank.email.worker.enabled=true"
        }
)
class EmailDeliveryWorkerServiceIntegrationTest {

    @Autowired
    private EmailDeliveryWorkerService workerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RecordingTransport transport;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        transport.reset();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
        transport.reset();
    }

    @Test
    void shouldSendOutsideTransactionAndMarkDeliverySent() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        Fixture fixture =
                insertFixture(
                        now.minusSeconds(5),
                        true
                );

        EmailDeliveryWorkerService.ProcessingOutcome result =
                workerService.processNext(
                        now
                );

        assertThat(result)
                .isEqualTo(
                        EmailDeliveryWorkerService
                                .ProcessingOutcome.SENT
                );

        assertThat(transport.messages)
                .hasSize(1);

        EmailDeliveryMessage message =
                transport.messages.getFirst();

        assertThat(message.deliveryId())
                .isEqualTo(
                        fixture.deliveryId()
                );

        assertThat(message.notificationId())
                .isEqualTo(
                        fixture.notificationId()
                );

        assertThat(message.recipientUserId())
                .isEqualTo(
                        fixture.recipientUserId()
                );

        assertThat(message.recipientEmail())
                .isEqualTo(
                        "worker@example.com"
                );

        assertThat(message.amount())
                .isEqualByComparingTo(
                        "10.0000"
                );

        assertThat(message.currency())
                .isEqualTo(
                        "USD"
                );

        assertThat(
                transport.transactionActiveDuringSend
        ).containsExactly(
                false
        );

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "SENT"
        );

        assertThat(attemptCountOf(
                fixture.deliveryId()
        )).isEqualTo(1);

        assertThat(claimTokenOf(
                fixture.deliveryId()
        )).isNull();

        assertThat(leaseUntilOf(
                fixture.deliveryId()
        )).isNull();

        assertThat(sentAtOf(
                fixture.deliveryId()
        )).isNotNull();
    }

    @Test
    void shouldRetryWhenRecipientContactHasNotArrivedYet() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        Fixture fixture =
                insertFixture(
                        now.minusSeconds(5),
                        false
                );

        EmailDeliveryWorkerService.ProcessingOutcome result =
                workerService.processNext(
                        now
                );

        assertThat(result)
                .isEqualTo(
                        EmailDeliveryWorkerService
                                .ProcessingOutcome.RETRY_SCHEDULED
                );

        assertThat(transport.messages)
                .isEmpty();

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PENDING"
        );

        assertThat(attemptCountOf(
                fixture.deliveryId()
        )).isEqualTo(1);

        assertThat(nextAttemptAtOf(
                fixture.deliveryId()
        )).isEqualTo(
                now.plusSeconds(30)
        );

        assertThat(lastErrorOf(
                fixture.deliveryId()
        )).isEqualTo(
                "Recipient contact not available"
        );
    }

    @Test
    void shouldRetryAfterTransportFailure() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        Fixture fixture =
                insertFixture(
                        now.minusSeconds(5),
                        true
                );

        transport.failure =
                new IllegalStateException(
                        "mail endpoint unavailable"
                );

        EmailDeliveryWorkerService.ProcessingOutcome result =
                workerService.processNext(
                        now
                );

        assertThat(result)
                .isEqualTo(
                        EmailDeliveryWorkerService
                                .ProcessingOutcome.RETRY_SCHEDULED
                );

        assertThat(transport.messages)
                .hasSize(1);

        assertThat(
                transport.transactionActiveDuringSend
        ).containsExactly(
                false
        );

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PENDING"
        );

        assertThat(nextAttemptAtOf(
                fixture.deliveryId()
        )).isEqualTo(
                now.plusSeconds(30)
        );

        assertThat(lastErrorOf(
                fixture.deliveryId()
        )).contains(
                "IllegalStateException"
        ).contains(
                "mail endpoint unavailable"
        );

        assertThat(sentAtOf(
                fixture.deliveryId()
        )).isNull();
    }

    @Test
    void shouldReturnNoWorkWhenNothingIsDue() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        EmailDeliveryWorkerService.ProcessingOutcome result =
                workerService.processNext(
                        now
                );

        assertThat(result)
                .isEqualTo(
                        EmailDeliveryWorkerService
                                .ProcessingOutcome.NO_WORK
                );

        assertThat(transport.messages)
                .isEmpty();
    }

    private Fixture insertFixture(
            Instant nextAttemptAt,
            boolean includeContact
    ) {
        UUID transferEventId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID recipientUserId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        UUID deliveryId =
                UUID.randomUUID();

        UUID journalId =
                UUID.randomUUID();

        UUID sourceAccountId =
                UUID.randomUUID();

        UUID destinationAccountId =
                UUID.randomUUID();

        Instant occurredAt =
                nextAttemptAt.minusSeconds(30);

        jdbcTemplate.update(
                """
                INSERT INTO inbox_events (
                    event_id,
                    event_type,
                    event_version,
                    aggregate_type,
                    aggregate_id,
                    occurred_at,
                    payload,
                    received_at,
                    processed_at
                ) VALUES (
                    ?,
                    'TRANSFER_POSTED',
                    2,
                    'TRANSFER',
                    ?,
                    ?,
                    '{}',
                    ?,
                    ?
                )
                """,
                transferEventId,
                requestId,
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt)
        );

        jdbcTemplate.update(
                """
                INSERT INTO transfer_notifications (
                    id,
                    event_id,
                    transfer_request_id,
                    actor_user_id,
                    journal_id,
                    source_account_id,
                    destination_account_id,
                    amount,
                    currency,
                    posted_at,
                    created_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?
                )
                """,
                notificationId,
                transferEventId,
                requestId,
                recipientUserId,
                journalId,
                sourceAccountId,
                destinationAccountId,
                new BigDecimal("10.0000"),
                "USD",
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt)
        );

        if (includeContact) {
            UUID contactEventId =
                    UUID.randomUUID();

            jdbcTemplate.update(
                    """
                    INSERT INTO inbox_events (
                        event_id,
                        event_type,
                        event_version,
                        aggregate_type,
                        aggregate_id,
                        occurred_at,
                        payload,
                        received_at,
                        processed_at
                    ) VALUES (
                        ?,
                        'USER_CONTACT_REGISTERED',
                        1,
                        'AUTH_USER',
                        ?,
                        ?,
                        '{}',
                        ?,
                        ?
                    )
                    """,
                    contactEventId,
                    recipientUserId,
                    Timestamp.from(occurredAt),
                    Timestamp.from(occurredAt),
                    Timestamp.from(occurredAt)
            );

            jdbcTemplate.update(
                    """
                    INSERT INTO notification_recipient_contacts (
                        user_id,
                        email,
                        source_event_id,
                        updated_at
                    ) VALUES (
                        ?, ?, ?, ?
                    )
                    """,
                    recipientUserId,
                    "worker@example.com",
                    contactEventId,
                    Timestamp.from(occurredAt)
            );
        }

        jdbcTemplate.update(
                """
                INSERT INTO email_deliveries (
                    id,
                    notification_id,
                    recipient_user_id,
                    status,
                    attempt_count,
                    next_attempt_at,
                    created_at,
                    updated_at
                ) VALUES (
                    ?, ?, ?,
                    'PENDING',
                    0,
                    ?, ?, ?
                )
                """,
                deliveryId,
                notificationId,
                recipientUserId,
                Timestamp.from(nextAttemptAt),
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt)
        );

        return new Fixture(
                deliveryId,
                notificationId,
                recipientUserId
        );
    }

    private String statusOf(
            UUID deliveryId
    ) {
        return jdbcTemplate.queryForObject(
                """
                SELECT status
                FROM email_deliveries
                WHERE id = ?
                """,
                String.class,
                deliveryId
        );
    }

    private int attemptCountOf(
            UUID deliveryId
    ) {
        Integer result =
                jdbcTemplate.queryForObject(
                        """
                        SELECT attempt_count
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Integer.class,
                        deliveryId
                );

        return result == null
                ? -1
                : result;
    }

    private UUID claimTokenOf(
            UUID deliveryId
    ) {
        return jdbcTemplate.queryForObject(
                """
                SELECT claim_token
                FROM email_deliveries
                WHERE id = ?
                """,
                UUID.class,
                deliveryId
        );
    }

    private Instant leaseUntilOf(
            UUID deliveryId
    ) {
        return toInstant(
                jdbcTemplate.queryForObject(
                        """
                        SELECT lease_until
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Timestamp.class,
                        deliveryId
                )
        );
    }

    private Instant sentAtOf(
            UUID deliveryId
    ) {
        return toInstant(
                jdbcTemplate.queryForObject(
                        """
                        SELECT sent_at
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Timestamp.class,
                        deliveryId
                )
        );
    }

    private Instant nextAttemptAtOf(
            UUID deliveryId
    ) {
        return toInstant(
                jdbcTemplate.queryForObject(
                        """
                        SELECT next_attempt_at
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Timestamp.class,
                        deliveryId
                )
        );
    }

    private String lastErrorOf(
            UUID deliveryId
    ) {
        return jdbcTemplate.queryForObject(
                """
                SELECT last_error
                FROM email_deliveries
                WHERE id = ?
                """,
                String.class,
                deliveryId
        );
    }

    private static Instant toInstant(
            Timestamp value
    ) {
        return value == null
                ? null
                : value.toInstant();
    }

    private void cleanDatabase() {
        jdbcTemplate.update(
                "DELETE FROM email_deliveries"
        );

        jdbcTemplate.update(
                "DELETE FROM transfer_notifications"
        );

        jdbcTemplate.update(
                "DELETE FROM notification_recipient_contacts"
        );

        jdbcTemplate.update(
                "DELETE FROM inbox_events"
        );
    }

    private record Fixture(
            UUID deliveryId,
            UUID notificationId,
            UUID recipientUserId
    ) {
    }

    @TestConfiguration
    static class TransportConfiguration {

        @Bean
        RecordingTransport emailDeliveryTransport() {
            return new RecordingTransport();
        }
    }

    static class RecordingTransport
            implements EmailDeliveryTransport {

        private final List<EmailDeliveryMessage>
                messages =
                new ArrayList<>();

        private final List<Boolean>
                transactionActiveDuringSend =
                new ArrayList<>();

        private RuntimeException failure;

        @Override
        public void send(
                EmailDeliveryMessage message
        ) {
            messages.add(
                    message
            );

            transactionActiveDuringSend.add(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            if (failure != null) {
                throw failure;
            }
        }

        void reset() {
            messages.clear();
            transactionActiveDuringSend.clear();
            failure = null;
        }
    }
}
