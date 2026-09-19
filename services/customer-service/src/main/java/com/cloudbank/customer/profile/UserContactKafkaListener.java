package com.cloudbank.customer.profile;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "cloudbank.kafka.consumer.enabled",
        havingValue = "true"
)
public class UserContactKafkaListener {

    private final CustomerProfileService service;

    public UserContactKafkaListener(CustomerProfileService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "${cloudbank.kafka.topics.user-contact}",
            groupId = "${cloudbank.kafka.consumer.group-id}"
    )
    public void consume(String message, Acknowledgment acknowledgment) {
        service.createFromRegistrationEvent(message);
        acknowledgment.acknowledge();
    }
}
