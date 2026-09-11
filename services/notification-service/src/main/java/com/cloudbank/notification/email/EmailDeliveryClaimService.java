package com.cloudbank.notification.email;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailDeliveryClaimService {

    static final Duration DEFAULT_LEASE_DURATION =
            Duration.ofSeconds(30);

    private final JdbcTemplate jdbcTemplate;

    public EmailDeliveryClaimService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate
                );
    }

    @Transactional
    public Optional<EmailDeliveryClaim> claimNext() {
        return claimNext(
                Instant.now(),
                DEFAULT_LEASE_DURATION
        );
    }

    @Transactional
    public Optional<EmailDeliveryClaim> claimNext(
            Instant now,
            Duration leaseDuration
    ) {
        Objects.requireNonNull(
                now,
                "Claim time is required"
        );

        Objects.requireNonNull(
                leaseDuration,
                "Lease duration is required"
        );

        if (leaseDuration.isZero()
                || leaseDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Lease duration must be positive"
            );
        }

        UUID claimToken =
                UUID.randomUUID();

        Instant leaseUntil =
                now.plus(
                        leaseDuration
                );

        List<EmailDeliveryClaim> claims =
                jdbcTemplate.query(
                        """
                        WITH candidate AS (
                            SELECT id
                            FROM email_deliveries
                            WHERE status = 'PENDING'
                              AND next_attempt_at <= ?
                            ORDER BY
                                next_attempt_at,
                                created_at,
                                id
                            LIMIT 1
                            FOR UPDATE SKIP LOCKED
                        )
                        UPDATE email_deliveries AS delivery
                        SET
                            status = 'PROCESSING',
                            claim_token = ?,
                            lease_until = ?,
                            attempt_count =
                                delivery.attempt_count + 1,
                            updated_at = ?
                        FROM candidate
                        WHERE delivery.id = candidate.id
                        RETURNING
                            delivery.id,
                            delivery.notification_id,
                            delivery.recipient_user_id,
                            delivery.claim_token,
                            delivery.lease_until,
                            delivery.attempt_count
                        """,
                        (resultSet, rowNumber) ->
                                new EmailDeliveryClaim(
                                        resultSet.getObject(
                                                "id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "notification_id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "recipient_user_id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "claim_token",
                                                UUID.class
                                        ),
                                        resultSet.getTimestamp(
                                                "lease_until"
                                        ).toInstant(),
                                        resultSet.getInt(
                                                "attempt_count"
                                        )
                                ),
                        Timestamp.from(
                                now
                        ),
                        claimToken,
                        Timestamp.from(
                                leaseUntil
                        ),
                        Timestamp.from(
                                now
                        )
                );

        if (claims.size() > 1) {
            throw new IllegalStateException(
                    "Claim operation returned multiple deliveries"
            );
        }

        return claims.stream()
                .findFirst();
    }
}
