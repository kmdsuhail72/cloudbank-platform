package com.cloudbank.notification.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
@ConditionalOnProperty(
        prefix = "cloudbank.email.worker",
        name = "enabled",
        havingValue = "true"
)
public class EmailDeliveryWorkerScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailDeliveryWorkerScheduler.class
            );

    private final EmailDeliveryClaimService
            claimService;

    private final EmailDeliveryWorkerService
            workerService;

    private final EmailDeliveryMetrics
            metrics;

    public EmailDeliveryWorkerScheduler(
            EmailDeliveryClaimService claimService,
            EmailDeliveryWorkerService workerService,
            EmailDeliveryMetrics metrics
    ) {
        this.claimService =
                Objects.requireNonNull(
                        claimService
                );

        this.workerService =
                Objects.requireNonNull(
                        workerService
                );

        this.metrics =
                Objects.requireNonNull(
                        metrics
                );
    }

    @Scheduled(
            fixedDelayString =
                    "${cloudbank.email.worker.poll-delay-ms:1000}"
    )
    public void poll() {
        int recovered =
                claimService.recoverExpiredLeases(
                        Instant.now()
                );

        metrics.recordRecoveredLeases(
                recovered
        );

        if (recovered > 0) {
            LOGGER.warn(
                    "email_delivery_expired_leases_recovered count={}",
                    recovered
            );
        }

        EmailDeliveryWorkerService.ProcessingOutcome outcome =
                workerService.processNext();

        metrics.recordOutcome(
                outcome
        );
    }
}
