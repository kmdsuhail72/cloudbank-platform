package com.cloudbank.notification.email;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailDeliveryWorkerSchedulerTest {

    @Test
    void shouldRecoverExpiredLeasesBeforeProcessingAndRecordMetrics() {
        EmailDeliveryClaimService claimService =
                mock(
                        EmailDeliveryClaimService.class
                );

        EmailDeliveryWorkerService workerService =
                mock(
                        EmailDeliveryWorkerService.class
                );

        EmailDeliveryMetrics metrics =
                mock(
                        EmailDeliveryMetrics.class
                );

        when(
                claimService.recoverExpiredLeases(
                        any()
                )
        ).thenReturn(
                2
        );

        when(
                workerService.processNext()
        ).thenReturn(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.SENT
        );

        EmailDeliveryWorkerScheduler scheduler =
                new EmailDeliveryWorkerScheduler(
                        claimService,
                        workerService,
                        metrics
                );

        scheduler.poll();

        verify(
                metrics
        ).recordRecoveredLeases(
                2
        );

        verify(
                metrics
        ).recordOutcome(
                EmailDeliveryWorkerService
                        .ProcessingOutcome.SENT
        );

        InOrder order =
                inOrder(
                        claimService,
                        workerService
                );

        order.verify(
                claimService
        ).recoverExpiredLeases(
                any()
        );

        order.verify(
                workerService
        ).processNext();
    }
}
