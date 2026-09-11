package com.cloudbank.notification.email;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDeliveryRetryPolicyTest {

    private final EmailDeliveryRetryPolicy policy =
            new EmailDeliveryRetryPolicy();

    @Test
    void shouldUseThirtySecondsForFirstAttempt() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        assertThat(
                policy.nextAttemptAt(
                        now,
                        1
                )
        ).isEqualTo(
                now.plusSeconds(30)
        );
    }

    @Test
    void shouldBackOffExponentially() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        assertThat(
                policy.nextAttemptAt(
                        now,
                        2
                )
        ).isEqualTo(
                now.plusSeconds(60)
        );

        assertThat(
                policy.nextAttemptAt(
                        now,
                        3
                )
        ).isEqualTo(
                now.plusSeconds(120)
        );
    }

    @Test
    void shouldCapBackoffAtFifteenMinutes() {
        Instant now =
                Instant.parse(
                        "2026-09-11T15:00:00Z"
                );

        assertThat(
                policy.nextAttemptAt(
                        now,
                        20
                )
        ).isEqualTo(
                now.plusSeconds(900)
        );
    }
}
