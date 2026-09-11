package com.cloudbank.notification.email;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailDeliveryWorkerSchedulerTest {

    @Test
    void shouldRecoverExpiredLeasesBeforeProcessingNext() {
        EmailDeliveryClaimService claimService =
                mock(
                        EmailDeliveryClaimService.class
                );

        EmailDeliveryWorkerService workerService =
                mock(
                        EmailDeliveryWorkerService.class
                );

        EmailDeliveryWorkerScheduler scheduler =
                new EmailDeliveryWorkerScheduler(
                        claimService,
                        workerService
                );

        scheduler.poll();

        verify(
                claimService
        ).recoverExpiredLeases(
                org.mockito.ArgumentMatchers.any()
        );

        verify(
                workerService
        ).processNext();
    }
}
