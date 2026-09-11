package com.cloudbank.notification.email;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EmailDeliveryMessage(
        UUID deliveryId,
        UUID notificationId,
        UUID recipientUserId,
        String recipientEmail,
        UUID transferRequestId,
        UUID journalId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        Instant postedAt
) {

    public EmailDeliveryMessage {
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

        if (recipientEmail == null
                || recipientEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required"
            );
        }

        Objects.requireNonNull(
                transferRequestId,
                "Transfer request ID is required"
        );

        Objects.requireNonNull(
                journalId,
                "Journal ID is required"
        );

        Objects.requireNonNull(
                sourceAccountId,
                "Source account ID is required"
        );

        Objects.requireNonNull(
                destinationAccountId,
                "Destination account ID is required"
        );

        Objects.requireNonNull(
                amount,
                "Amount is required"
        );

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        if (currency == null
                || !currency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException(
                    "Currency must be three uppercase letters"
            );
        }

        Objects.requireNonNull(
                postedAt,
                "Posted time is required"
        );
    }
}
