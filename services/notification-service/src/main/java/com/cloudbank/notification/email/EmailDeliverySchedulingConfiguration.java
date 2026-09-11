package com.cloudbank.notification.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "cloudbank.email.worker",
        name = "enabled",
        havingValue = "true"
)
public class EmailDeliverySchedulingConfiguration {
}
