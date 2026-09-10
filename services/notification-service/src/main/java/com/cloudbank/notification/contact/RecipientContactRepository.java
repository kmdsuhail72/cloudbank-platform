package com.cloudbank.notification.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipientContactRepository
        extends JpaRepository<RecipientContact, UUID> {
}
