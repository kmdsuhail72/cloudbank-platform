package com.cloudbank.notification.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboxEventRepository
        extends JpaRepository<InboxEvent, UUID> {
}
