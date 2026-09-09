package com.cloudbank.notification.transfer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.Acknowledgment;

import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "cloudbank.kafka.consumer.enabled",
        havingValue = "true"
)
public class TransferNotificationKafkaListener {

    private final TransferNotificationProcessingService
            processingService;

    public TransferNotificationKafkaListener(
            TransferNotificationProcessingService processingService
    ) {
        this.processingService =
                processingService;
    }

    @KafkaListener(
            topics = "${cloudbank.kafka.topics.transfer}",
            groupId = "${cloudbank.kafka.consumer.group-id}"
    )
    public void consume(
            String message,
            Acknowledgment acknowledgment
    ) {
        processingService.process(
                message
        );

        /*
         * process() owns the database transaction.
         * Reaching this line means that transaction committed.
         */
        acknowledgment.acknowledge();
    }
}
