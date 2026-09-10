package com.cloudbank.notification.contact;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "cloudbank.kafka.contact-consumer.enabled",
        havingValue = "true"
)
public class RecipientContactKafkaListener {

    private final RecipientContactProcessingService
            processingService;

    public RecipientContactKafkaListener(
            RecipientContactProcessingService processingService
    ) {
        this.processingService =
                processingService;
    }

    @KafkaListener(
            topics =
                    "${cloudbank.kafka.topics.user-contact}",
            groupId =
                    "${cloudbank.kafka.contact-consumer.group-id}"
    )
    public void consume(
            String message,
            Acknowledgment acknowledgment
    ) {
        processingService.process(
                message
        );

        acknowledgment.acknowledge();
    }
}
