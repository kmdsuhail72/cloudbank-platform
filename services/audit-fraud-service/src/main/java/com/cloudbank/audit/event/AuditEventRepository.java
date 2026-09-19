package com.cloudbank.audit.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
