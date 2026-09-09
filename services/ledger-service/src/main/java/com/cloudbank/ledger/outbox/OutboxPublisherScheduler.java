package com.cloudbank.ledger.outbox;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "cloudbank.outbox.publisher.enabled",
        havingValue = "true"
)
public class OutboxPublisherScheduler {

    private final OutboxPublishingService publishingService;

    private final int batchSize;

    public OutboxPublisherScheduler(
            OutboxPublishingService publishingService,
            @Value(
                    "${cloudbank.outbox.publisher.batch-size}"
            )
            int batchSize
    ) {
        this.publishingService =
                publishingService;

        if (batchSize < 1) {
            throw new IllegalArgumentException(
                    "Outbox batch size must be positive"
            );
        }

        this.batchSize =
                batchSize;
    }

    @Scheduled(
            fixedDelayString =
                    "${cloudbank.outbox.publisher.fixed-delay-ms}"
    )
    public void publishPendingEvents() {
        for (
                int index = 0;
                index < batchSize;
                index++
        ) {
            OutboxPublishingService.PublishOutcome
                    outcome =
                    publishingService
                            .publishNextPending();

            if (
                    outcome
                    != OutboxPublishingService
                            .PublishOutcome
                            .PUBLISHED
            ) {
                /*
                 * NONE: there is no more work.
                 *
                 * FAILED: do not hammer the same pending event
                 * repeatedly during this scheduler invocation.
                 * The next scheduled run will retry it.
                 */
                return;
            }
        }
    }
}
