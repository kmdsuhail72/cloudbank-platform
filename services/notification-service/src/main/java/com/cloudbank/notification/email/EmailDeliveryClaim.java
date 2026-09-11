package com.cloudbank.notification.email;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EmailDeliveryClaim(
        UUID deliveryId,
        UUID notificationId,
        UUID recipientUserId,
        UUID claimToken,
        Instant leaseUntil,
        int attemptCount
) {

    public EmailDeliveryClaim {
        Objects.requireNonNull(
                deliveryId,
                "Delivery ID is required"
        );

        Objects.requireNonNull(
                notificationId,
                "Notification ID is required"
        );

        Objects.requireNonNull(
                recipientUserId,
                "Recipient user ID is required"
        );

        Objects.requireNonNull(
                claimToken,
                "Claim token is required"
        );

        Objects.requireNonNull(
                leaseUntil,
                "Lease expiry is required"
        );

        if (attemptCount <= 0) {
            throw new IllegalArgumentException(
                    "Attempt count must be positive"
            );
        }
    }
}
