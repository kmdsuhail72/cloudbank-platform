package com.cloudbank.ledger.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    Optional<OutboxEvent>
    findByAggregateTypeAndAggregateIdAndEventType(
            String aggregateType,
            UUID aggregateId,
            String eventType
    );

    long countByPublishedAtIsNull();

    @Query(
            value = """
                    SELECT *
                    FROM outbox_events
                    WHERE published_at IS NULL
                    ORDER BY created_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<OutboxEvent> claimNextPending();
}
