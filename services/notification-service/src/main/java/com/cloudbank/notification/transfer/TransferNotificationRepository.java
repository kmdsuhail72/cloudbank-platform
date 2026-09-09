package com.cloudbank.notification.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferNotificationRepository
        extends JpaRepository<TransferNotification, UUID> {

    long countByEventId(
            UUID eventId
    );
}
