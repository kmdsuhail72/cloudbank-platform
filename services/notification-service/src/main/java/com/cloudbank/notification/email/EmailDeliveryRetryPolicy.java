package com.cloudbank.notification.email;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class EmailDeliveryRetryPolicy {

    static final Duration BASE_DELAY =
            Duration.ofSeconds(30);

    static final Duration MAX_DELAY =
            Duration.ofMinutes(15);

    public Instant nextAttemptAt(
            Instant now,
            int attemptCount
    ) {
        Objects.requireNonNull(
                now,
                "Retry calculation time is required"
        );

        if (attemptCount <= 0) {
            throw new IllegalArgumentException(
                    "Attempt count must be positive"
            );
        }

        Duration delay =
                BASE_DELAY;

        for (int i = 1;
             i < attemptCount
                     && delay.compareTo(MAX_DELAY) < 0;
             i++) {

            delay =
                    delay.multipliedBy(2);

            if (delay.compareTo(MAX_DELAY) > 0) {
                delay =
                        MAX_DELAY;
            }
        }

        return now.plus(
                delay
        );
    }
}
