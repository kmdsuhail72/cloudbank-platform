package com.cloudbank.notification.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailDeliveryRepository
        extends JpaRepository<EmailDelivery, UUID> {

    long countByNotificationId(
            UUID notificationId
    );
}
