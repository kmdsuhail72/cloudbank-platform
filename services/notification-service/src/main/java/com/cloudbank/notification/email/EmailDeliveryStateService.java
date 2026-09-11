package com.cloudbank.notification.email;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class EmailDeliveryStateService {

    private final JdbcTemplate jdbcTemplate;

    public EmailDeliveryStateService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate
                );
    }

    @Transactional
    public boolean markSent(
            UUID deliveryId,
            UUID claimToken,
            Instant sentAt
    ) {
        Objects.requireNonNull(
                deliveryId,
                "Delivery ID is required"
        );

        Objects.requireNonNull(
                claimToken,
                "Claim token is required"
        );

        Objects.requireNonNull(
                sentAt,
                "Sent time is required"
        );

        int updated =
                jdbcTemplate.update(
                        """
                        UPDATE email_deliveries
                        SET
                            status = 'SENT',
                            claim_token = NULL,
                            lease_until = NULL,
                            last_error = NULL,
                            sent_at = ?,
                            updated_at = ?
                        WHERE id = ?
                          AND status = 'PROCESSING'
                          AND claim_token = ?
                        """,
                        Timestamp.from(
                                sentAt
                        ),
                        Timestamp.from(
                                sentAt
                        ),
                        deliveryId,
                        claimToken
                );

        return updated == 1;
    }

    @Transactional
    public boolean releaseForRetry(
            UUID deliveryId,
            UUID claimToken,
            Instant now,
            Instant nextAttemptAt,
            String error
    ) {
        Objects.requireNonNull(
                deliveryId,
                "Delivery ID is required"
        );

        Objects.requireNonNull(
                claimToken,
                "Claim token is required"
        );

        Objects.requireNonNull(
                now,
                "Retry update time is required"
        );

        Objects.requireNonNull(
                nextAttemptAt,
                "Next attempt time is required"
        );

        if (nextAttemptAt.isBefore(now)) {
            throw new IllegalArgumentException(
                    "Next attempt time cannot be before update time"
            );
        }

        if (error == null
                || error.isBlank()) {
            throw new IllegalArgumentException(
                    "Retry error is required"
            );
        }

        int updated =
                jdbcTemplate.update(
                        """
                        UPDATE email_deliveries
                        SET
                            status = 'PENDING',
                            claim_token = NULL,
                            lease_until = NULL,
                            next_attempt_at = ?,
                            last_error = ?,
                            sent_at = NULL,
                            updated_at = ?
                        WHERE id = ?
                          AND status = 'PROCESSING'
                          AND claim_token = ?
                        """,
                        Timestamp.from(
                                nextAttemptAt
                        ),
                        error,
                        Timestamp.from(
                                now
                        ),
                        deliveryId,
                        claimToken
                );

        return updated == 1;
    }
}
