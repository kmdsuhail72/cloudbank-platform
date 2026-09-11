package com.cloudbank.notification.email;

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

        EmailDeliveryWorkerService.ProcessingOutcome outcome =
                workerService.processNext();

        metrics.recordOutcome(
                outcome
        );
    }
}
