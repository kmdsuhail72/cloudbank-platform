package com.cloudbank.notification.email;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailDeliveryMetricsTest {

    @Test
    void shouldRecordWorkerOutcomesAndRecoveredLeases() {
        SimpleMeterRegistry registry =
                new SimpleMeterRegistry();

        JdbcTemplate jdbcTemplate =
                mock(
                        JdbcTemplate.class
                );

        EmailDeliveryMetrics metrics =
                new EmailDeliveryMetrics(
                        registry,
                        jdbcTemplate
                );

        metrics.recordOutcome(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.SENT
        );

        metrics.recordOutcome(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.RETRY_SCHEDULED
        );

        metrics.recordOutcome(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.RETRY_SCHEDULED
        );

        metrics.recordOutcome(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.FAILED
        );

        metrics.recordRecoveredLeases(
                3
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .WORKER_OUTCOMES
                        )
                        .tag(
                                "outcome",
                                "sent"
                        )
                        .counter()
                        .count()
        ).isEqualTo(
                1.0
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .WORKER_OUTCOMES
                        )
                        .tag(
                                "outcome",
                                "retry_scheduled"
                        )
                        .counter()
                        .count()
        ).isEqualTo(
                2.0
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .WORKER_OUTCOMES
                        )
                        .tag(
                                "outcome",
                                "failed"
                        )
                        .counter()
                        .count()
        ).isEqualTo(
                1.0
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .RECOVERED_LEASES
                        )
                        .counter()
                        .count()
        ).isEqualTo(
                3.0
        );
    }

    @Test
    void shouldReadDeliveryStateGaugeFromDatabase() {
        SimpleMeterRegistry registry =
                new SimpleMeterRegistry();

        JdbcTemplate jdbcTemplate =
                mock(
                        JdbcTemplate.class
                );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq("FAILED")
                )
        ).thenReturn(
                2L
        );

        EmailDeliveryMetrics metrics =
                new EmailDeliveryMetrics(
                        registry,
                        jdbcTemplate
                );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .DELIVERY_STATES
                        )
                        .tag(
                                "status",
                                "FAILED"
                        )
                        .gauge()
                        .value()
        ).isEqualTo(
                2.0
        );
    }

    @Test
    void shouldExposeActionableDeliveryAges() {
        SimpleMeterRegistry registry =
                new SimpleMeterRegistry();

        JdbcTemplate jdbcTemplate =
                mock(
                        JdbcTemplate.class
                );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Double.class)
                )
        ).thenReturn(
                45.0,
                12.0
        );

        new EmailDeliveryMetrics(
                registry,
                jdbcTemplate
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .PENDING_OLDEST_DUE_SECONDS
                        )
                        .gauge()
                        .value()
        ).isEqualTo(
                45.0
        );

        assertThat(
                registry.get(
                                EmailDeliveryMetrics
                                        .PROCESSING_OLDEST_EXPIRED_LEASE_SECONDS
                        )
                        .gauge()
                        .value()
        ).isEqualTo(
                12.0
        );
    }

    @Test
    void shouldRejectNegativeRecoveredLeaseCount() {
        EmailDeliveryMetrics metrics =
                new EmailDeliveryMetrics(
                        new SimpleMeterRegistry(),
                        mock(
                                JdbcTemplate.class
                        )
                );

        assertThatThrownBy(
                () -> metrics.recordRecoveredLeases(
                        -1
                )
        ).isInstanceOf(
                IllegalArgumentException.class
        );
    }
}
