package com.cloudbank.notification.email;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class EmailDeliveryMetrics {

    static final String WORKER_OUTCOMES =
            "cloudbank.email.worker.outcomes";

    static final String RECOVERED_LEASES =
            "cloudbank.email.leases.recovered";

    static final String DELIVERY_STATES =
            "cloudbank.email.deliveries";

    static final String PENDING_OLDEST_DUE_SECONDS =
            "cloudbank.email.pending.oldest.due.seconds";

    static final String PROCESSING_OLDEST_EXPIRED_LEASE_SECONDS =
            "cloudbank.email.processing.oldest.expired.lease.seconds";

    private static final List<String> DELIVERY_STATUSES =
            List.of(
                    "PENDING",
                    "PROCESSING",
                    "SENT",
                    "FAILED"
            );

    private final JdbcTemplate jdbcTemplate;

    private final Map<
            EmailDeliveryWorkerService.ProcessingOutcome,
            Counter
            > outcomeCounters =
            new EnumMap<>(
                    EmailDeliveryWorkerService
                            .ProcessingOutcome.class
            );

    private final Counter recoveredLeases;

    public EmailDeliveryMetrics(
            MeterRegistry meterRegistry,
            JdbcTemplate jdbcTemplate
    ) {
        Objects.requireNonNull(
                meterRegistry,
                "Meter registry is required"
        );

        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate,
                        "JdbcTemplate is required"
                );

        for (
                EmailDeliveryWorkerService.ProcessingOutcome outcome
                : EmailDeliveryWorkerService
                        .ProcessingOutcome.values()
        ) {
            Counter counter =
                    Counter.builder(
                                    WORKER_OUTCOMES
                            )
                            .description(
                                    "Email delivery worker outcomes"
                            )
                            .tag(
                                    "outcome",
                                    outcome.name()
                                            .toLowerCase(
                                                    Locale.ROOT
                                            )
                            )
                            .register(
                                    meterRegistry
                            );

            outcomeCounters.put(
                    outcome,
                    counter
            );
        }

        recoveredLeases =
                Counter.builder(
                                RECOVERED_LEASES
                        )
                        .description(
                                "Expired email delivery leases recovered"
                        )
                        .register(
                                meterRegistry
                        );

        for (String status : DELIVERY_STATUSES) {
            Gauge.builder(
                            DELIVERY_STATES,
                            this,
                            ignored ->
                                    countDeliveriesByStatus(
                                            status
                                    )
                    )
                    .description(
                            "Current email delivery rows by status"
                    )
                    .tag(
                            "status",
                            status
                    )
                    .register(
                            meterRegistry
                    );
        }

        Gauge.builder(
                        PENDING_OLDEST_DUE_SECONDS,
                        this,
                        ignored ->
                                oldestPendingDueSeconds()
                )
                .description(
                        "Age in seconds of the oldest due pending email delivery"
                )
                .register(
                        meterRegistry
                );

        Gauge.builder(
                        PROCESSING_OLDEST_EXPIRED_LEASE_SECONDS,
                        this,
                        ignored ->
                                oldestExpiredProcessingLeaseSeconds()
                )
                .description(
                        "Age in seconds of the oldest expired processing lease"
                )
                .register(
                        meterRegistry
                );
    }

    public void recordOutcome(
            EmailDeliveryWorkerService.ProcessingOutcome outcome
    ) {
        Objects.requireNonNull(
                outcome,
                "Processing outcome is required"
        );

        outcomeCounters.get(
                outcome
        ).increment();
    }

    public void recordRecoveredLeases(
            int recovered
    ) {
        if (recovered < 0) {
            throw new IllegalArgumentException(
                    "Recovered lease count cannot be negative"
            );
        }

        if (recovered > 0) {
            recoveredLeases.increment(
                    recovered
            );
        }
    }

    private double countDeliveriesByStatus(
            String status
    ) {
        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT count(*)
                        FROM email_deliveries
                        WHERE status = ?
                        """,
                        Long.class,
                        status
                );

        return count == null
                ? 0.0
                : count.doubleValue();
    }

    private double oldestPendingDueSeconds() {
        Double age =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COALESCE(
                            EXTRACT(
                                EPOCH FROM (
                                    CURRENT_TIMESTAMP
                                    - MIN(next_attempt_at)
                                )
                            ),
                            0
                        )::double precision
                        FROM email_deliveries
                        WHERE status = 'PENDING'
                          AND next_attempt_at
                              <= CURRENT_TIMESTAMP
                        """,
                        Double.class
                );

        return age == null
                ? 0.0
                : Math.max(
                        age,
                        0.0
                );
    }

    private double oldestExpiredProcessingLeaseSeconds() {
        Double age =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COALESCE(
                            EXTRACT(
                                EPOCH FROM (
                                    CURRENT_TIMESTAMP
                                    - MIN(lease_until)
                                )
                            ),
                            0
                        )::double precision
                        FROM email_deliveries
                        WHERE status = 'PROCESSING'
                          AND lease_until
                              < CURRENT_TIMESTAMP
                        """,
                        Double.class
                );

        return age == null
                ? 0.0
                : Math.max(
                        age,
                        0.0
                );
    }
}
