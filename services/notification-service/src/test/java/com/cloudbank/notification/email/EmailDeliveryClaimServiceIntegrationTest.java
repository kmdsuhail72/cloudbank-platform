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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "cloudbank.kafka.consumer.enabled=false",
                "cloudbank.kafka.contact-consumer.enabled=false"
        }
)
class EmailDeliveryClaimServiceIntegrationTest {

    @Autowired
    private EmailDeliveryClaimService claimService;

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
    void shouldClaimOldestDuePendingDelivery() {
        Instant now =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture oldest =
                insertPendingDelivery(
                        now.minusSeconds(120)
                );

        DeliveryFixture newer =
                insertPendingDelivery(
                        now.minusSeconds(60)
                );

        Optional<EmailDeliveryClaim> result =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                );

        assertThat(result)
                .isPresent();

        EmailDeliveryClaim claim =
                result.orElseThrow();

        assertThat(claim.deliveryId())
                .isEqualTo(
                        oldest.deliveryId()
                );

        assertThat(claim.notificationId())
                .isEqualTo(
                        oldest.notificationId()
                );

        assertThat(claim.recipientUserId())
                .isEqualTo(
                        oldest.recipientUserId()
                );

        assertThat(claim.claimToken())
                .isNotNull();

        assertThat(claim.leaseUntil())
                .isEqualTo(
                        now.plusSeconds(30)
                );

        assertThat(claim.attemptCount())
                .isEqualTo(1);

        assertThat(statusOf(
                oldest.deliveryId()
        )).isEqualTo(
                "PROCESSING"
        );

        assertThat(statusOf(
                newer.deliveryId()
        )).isEqualTo(
                "PENDING"
        );

        assertThat(attemptCountOf(
                oldest.deliveryId()
        )).isEqualTo(1);

        assertThat(attemptCountOf(
                newer.deliveryId()
        )).isZero();
    }

    @Test
    void shouldNotClaimFutureDelivery() {
        Instant now =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture future =
                insertPendingDelivery(
                        now.plusSeconds(60)
                );

        Optional<EmailDeliveryClaim> result =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                );

        assertThat(result)
                .isEmpty();

        assertThat(statusOf(
                future.deliveryId()
        )).isEqualTo(
                "PENDING"
        );

        assertThat(attemptCountOf(
                future.deliveryId()
        )).isZero();
    }

    @Test
    void shouldNotClaimProcessingDeliveryAgain() {
        Instant now =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        now.minusSeconds(60)
                );

        Optional<EmailDeliveryClaim> first =
                claimService.claimNext(
                        now,
                        Duration.ofSeconds(30)
                );

        Optional<EmailDeliveryClaim> second =
                claimService.claimNext(
                        now.plusSeconds(1),
                        Duration.ofSeconds(30)
                );

        assertThat(first)
                .isPresent();

        assertThat(second)
                .isEmpty();

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PROCESSING"
        );

        assertThat(attemptCountOf(
                fixture.deliveryId()
        )).isEqualTo(1);
    }

    @Test
    void shouldRecoverExpiredProcessingLease() {
        Instant claimTime =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        claimTime.minusSeconds(60)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        claimTime,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        int recovered =
                claimService.recoverExpiredLeases(
                        claimTime.plusSeconds(31)
                );

        assertThat(recovered)
                .isEqualTo(1);

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PENDING"
        );

        assertThat(claimTokenOf(
                fixture.deliveryId()
        )).isNull();

        assertThat(leaseUntilOf(
                fixture.deliveryId()
        )).isNull();

        assertThat(nextAttemptAtOf(
                fixture.deliveryId()
        )).isEqualTo(
                claimTime.plusSeconds(31)
        );

        assertThat(attemptCountOf(
                fixture.deliveryId()
        )).isEqualTo(
                claim.attemptCount()
        );
    }

    @Test
    void shouldNotRecoverUnexpiredProcessingLease() {
        Instant claimTime =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        claimTime.minusSeconds(60)
                );

        EmailDeliveryClaim claim =
                claimService.claimNext(
                        claimTime,
                        Duration.ofSeconds(30)
                ).orElseThrow();

        int recovered =
                claimService.recoverExpiredLeases(
                        claimTime.plusSeconds(29)
                );

        assertThat(recovered)
                .isZero();

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PROCESSING"
        );

        assertThat(claimTokenOf(
                fixture.deliveryId()
        )).isEqualTo(
                claim.claimToken()
        );

        assertThat(leaseUntilOf(
                fixture.deliveryId()
        )).isEqualTo(
                claim.leaseUntil()
        );

        assertThat(attemptCountOf(
                fixture.deliveryId()
        )).isEqualTo(1);
    }

    @Test
    void shouldReclaimRecoveredDeliveryWithNewClaimToken() {
        Instant firstClaimTime =
                Instant.parse(
                        "2026-09-11T14:00:00Z"
                );

        DeliveryFixture fixture =
                insertPendingDelivery(
                        firstClaimTime.minusSeconds(60)
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

        assertThat(secondClaim.deliveryId())
                .isEqualTo(
                        fixture.deliveryId()
                );

        assertThat(secondClaim.claimToken())
                .isNotEqualTo(
                        firstClaim.claimToken()
                );

        assertThat(secondClaim.attemptCount())
                .isEqualTo(2);

        assertThat(statusOf(
                fixture.deliveryId()
        )).isEqualTo(
                "PROCESSING"
        );
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
                Timestamp.from(
                        occurredAt
                ),
                Timestamp.from(
                        occurredAt
                ),
                Timestamp.from(
                        occurredAt
                )
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
                new BigDecimal(
                        "10.0000"
                ),
                "USD",
                Timestamp.from(
                        occurredAt
                ),
                Timestamp.from(
                        occurredAt
                )
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
                Timestamp.from(
                        nextAttemptAt
                ),
                Timestamp.from(
                        occurredAt
                ),
                Timestamp.from(
                        occurredAt
                )
        );

        return new DeliveryFixture(
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
        Timestamp value =
                jdbcTemplate.queryForObject(
                        """
                        SELECT lease_until
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Timestamp.class,
                        deliveryId
                );

        return value == null
                ? null
                : value.toInstant();
    }

    private Instant nextAttemptAtOf(
            UUID deliveryId
    ) {
        Timestamp value =
                jdbcTemplate.queryForObject(
                        """
                        SELECT next_attempt_at
                        FROM email_deliveries
                        WHERE id = ?
                        """,
                        Timestamp.class,
                        deliveryId
                );

        return value == null
                ? null
                : value.toInstant();
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
            UUID deliveryId,
            UUID notificationId,
            UUID recipientUserId
    ) {
    }
}
