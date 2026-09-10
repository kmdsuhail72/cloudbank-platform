package com.cloudbank.auth.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuthOutboxEventRepository
        extends JpaRepository<AuthOutboxEvent, UUID> {

    Optional<AuthOutboxEvent>
    findByAggregateTypeAndAggregateIdAndEventType(
            String aggregateType,
            UUID aggregateId,
            String eventType
    );

    long countByPublishedAtIsNull();

    @Query(
            value = """
                    SELECT *
                    FROM auth_outbox_events
                    WHERE published_at IS NULL
                    ORDER BY created_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<AuthOutboxEvent> claimNextPending();
}
