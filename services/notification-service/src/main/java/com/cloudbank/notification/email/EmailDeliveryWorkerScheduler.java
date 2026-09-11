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

    public EmailDeliveryWorkerScheduler(
            EmailDeliveryClaimService claimService,
            EmailDeliveryWorkerService workerService
    ) {
        this.claimService =
                Objects.requireNonNull(
                        claimService
                );

        this.workerService =
                Objects.requireNonNull(
                        workerService
                );
    }

    @Scheduled(
            fixedDelayString =
                    "${cloudbank.email.worker.poll-delay-ms:1000}"
    )
    public void poll() {
        claimService.recoverExpiredLeases(
                Instant.now()
        );

        workerService.processNext();
    }
}
