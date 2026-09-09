package com.cloudbank.ledger.outbox;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository repository;

    @Test
    void shouldPersistPendingOutboxEvent() {
        UUID transferId =
                UUID.randomUUID();

        String payload =
                """
                {
                  "requestId":"%s",
                  "amount":25.0000,
                  "currency":"INR"
                }
                """.formatted(
                        transferId
                );

        OutboxEvent persisted =
                repository.saveAndFlush(
                        new OutboxEvent(
                                "TRANSFER",
                                transferId,
                                "TRANSFER_POSTED",
                                1,
                                payload
                        )
                );

        assertNotNull(
                persisted.getId()
        );

        assertNotNull(
                persisted.getCreatedAt()
        );

        assertEquals(
                "TRANSFER",
                persisted.getAggregateType()
        );

        assertEquals(
                transferId,
                persisted.getAggregateId()
        );

        assertEquals(
                "TRANSFER_POSTED",
                persisted.getEventType()
        );

        assertEquals(
                1,
                persisted.getEventVersion()
        );

        assertEquals(
                0,
                persisted.getAttemptCount()
        );

        assertEquals(
                null,
                persisted.getPublishedAt()
        );

        assertEquals(
                null,
                persisted.getLastError()
        );

        assertTrue(
                repository
                        .findByAggregateTypeAndAggregateIdAndEventType(
                                "TRANSFER",
                                transferId,
                                "TRANSFER_POSTED"
                        )
                        .isPresent()
        );

        assertEquals(
                1,
                repository.countByPublishedAtIsNull()
        );
    }

    @Test
    void shouldRejectDuplicateAggregateEvent() {
        UUID transferId =
                UUID.randomUUID();

        repository.saveAndFlush(
                new OutboxEvent(
                        "TRANSFER",
                        transferId,
                        "TRANSFER_POSTED",
                        1,
                        "{\"sequence\":1}"
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(
                        new OutboxEvent(
                                "TRANSFER",
                                transferId,
                                "TRANSFER_POSTED",
                                1,
                                "{\"sequence\":2}"
                        )
                )
        );
    }
}
