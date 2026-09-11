package com.cloudbank.notification.email;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "cloudbank.kafka.consumer.enabled=false",
                "cloudbank.kafka.contact-consumer.enabled=false"
        }
)
class EmailDeliveryStateServiceIntegrationTest {

    @Autowired
    private EmailDeliveryClaimService claimService;

    @Autowired
    private EmailDeliveryStateService stateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldMarkSentOnlyWithCurrentClaimToken() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        now.minusSeconds(10)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        Instant sentAt =
                now.plusSeconds(5);

        boolean updated =
                stateService.markSent(
                        fixture.deliveryId(),
                        claim.claimToken(),
                        sentAt
                );

        assertThat(updated)
                .isTrue();

        DeliveryState state =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(state.status())
                .isEqualTo("SENT");

        assertThat(state.claimToken())
                .isNull();

        assertThat(state.leaseUntil())
                .isNull();

        assertThat(state.sentAt())
                .isEqualTo(sentAt);

        assertThat(state.lastError())
                .isNull();

        assertThat(state.attemptCount())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectMarkSentWithStaleClaimToken() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        now.minusSeconds(10)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        boolean updated =
                stateService.markSent(
                        fixture.deliveryId(),
                        UUID.randomUUID(),
                        now.plusSeconds(5)
                );

        assertThat(updated)
                .isFalse();

        DeliveryState state =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(state.status())
                .isEqualTo("PROCESSING");

        assertThat(state.claimToken())
                .isEqualTo(
                        claim.claimToken()
                );

        assertThat(state.sentAt())
                .isNull();
    }

    @Test
    void shouldReleaseClaimForRetryWithCurrentToken() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        now.minusSeconds(10)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        Instant failureTime =
                now.plusSeconds(5);

        Instant retryAt =
                failureTime.plusSeconds(60);

        boolean updated =
                stateService.releaseForRetry(
                        fixture.deliveryId(),
                        claim.claimToken(),
                        failureTime,
                        retryAt,
                        "SMTP temporarily unavailable"
                );

        assertThat(updated)
                .isTrue();

        DeliveryState state =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(state.status())
                .isEqualTo("PENDING");

        assertThat(state.claimToken())
                .isNull();

        assertThat(state.leaseUntil())
                .isNull();

        assertThat(state.sentAt())
                .isNull();

        assertThat(state.nextAttemptAt())
                .isEqualTo(retryAt);

        assertThat(state.lastError())
                .isEqualTo(
                        "SMTP temporarily unavailable"
                );

        assertThat(state.attemptCount())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectRetryReleaseWithStaleClaimToken() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        now.minusSeconds(10)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        boolean updated =
                stateService.releaseForRetry(
                        fixture.deliveryId(),
                        UUID.randomUUID(),
                        now.plusSeconds(5),
                        now.plusSeconds(65),
                        "temporary failure"
                );

        assertThat(updated)
                .isFalse();

        DeliveryState state =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(state.status())
                .isEqualTo("PROCESSING");

        assertThat(state.claimToken())
                .isEqualTo(
                        claim.claimToken()
                );

        assertThat(state.lastError())
                .isNull();
    }

    @Test
    void staleWorkerCannotFinalizeAfterLeaseRecoveryAndReclaim() {
        Instant firstClaimTime =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        firstClaimTime.minusSeconds(10)
                );

        EmailDeliveryClaim firstClaim =
                claimService.claimNext(
                        firstClaimTime,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        Instant recoveryTime =
                firstClaimTime.plusSeconds(31);

        assertThat(
                claimService.recoverExpiredLeases(
                        recoveryTime
                )
        ).isEqualTo(1);

        EmailDeliveryClaim secondClaim =
                claimService.claimNext(
                        recoveryTime,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        assertThat(secondClaim.claimToken())
                .isNotEqualTo(
                        firstClaim.claimToken()
                );

        assertThat(secondClaim.attemptCount())
                .isEqualTo(2);

        boolean staleResult =
                stateService.markSent(
                        fixture.deliveryId(),
                        firstClaim.claimToken(),
                        recoveryTime.plusSeconds(2)
                );

        assertThat(staleResult)
                .isFalse();

        DeliveryState stillOwnedBySecondWorker =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(stillOwnedBySecondWorker.status())
                .isEqualTo("PROCESSING");

        assertThat(stillOwnedBySecondWorker.claimToken())
                .isEqualTo(
                        secondClaim.claimToken()
                );

        boolean currentResult =
                stateService.markSent(
                        fixture.deliveryId(),
                        secondClaim.claimToken(),
                        recoveryTime.plusSeconds(3)
                );

        assertThat(currentResult)
                .isTrue();

        DeliveryState finalState =
                loadState(
                        fixture.deliveryId()
                );

        assertThat(finalState.status())
                .isEqualTo("SENT");

        assertThat(finalState.claimToken())
                .isNull();

        assertThat(finalState.leaseUntil())
                .isNull();
    }

    private DeliveryFixture insertPendingDelivery(
            Instant nextAttemptAt
    ) {
        UUID eventId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        UUID recipientUserId =
                UUID.randomUUID();

        UUID deliveryId =
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
                eventId,
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
                eventId,
                requestId,
                recipientUserId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10.0000"),
                "USD",
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt)
        );

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

        return new DeliveryFixture(
                deliveryId
        );
    }

    private DeliveryState loadState(
            UUID deliveryId
    ) {
        return jdbcTemplate.queryForObject(
                """
                SELECT
                    status,
                    attempt_count,
                    next_attempt_at,
                    claim_token,
                    lease_until,
                    last_error,
                    sent_at
                FROM email_deliveries
                WHERE id = ?
                """,
                (resultSet, rowNumber) ->
                        new DeliveryState(
                                resultSet.getString(
                                        "status"
                                ),
                                resultSet.getInt(
                                        "attempt_count"
                                ),
                                toInstant(
                                        resultSet.getTimestamp(
                                                "next_attempt_at"
                                        )
                                ),
                                resultSet.getObject(
                                        "claim_token",
                                        UUID.class
                                ),
                                toInstant(
                                        resultSet.getTimestamp(
                                                "lease_until"
                                        )
                                ),
                                resultSet.getString(
                                        "last_error"
                                ),
                                toInstant(
                                        resultSet.getTimestamp(
                                                "sent_at"
                                        )
                                )
                        ),
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

    private record DeliveryFixture(
            UUID deliveryId
    ) {
    }

    private record DeliveryState(
            String status,
            int attemptCount,
            Instant nextAttemptAt,
            UUID claimToken,
            Instant leaseUntil,
            String lastError,
            Instant sentAt
    ) {
    }
}
