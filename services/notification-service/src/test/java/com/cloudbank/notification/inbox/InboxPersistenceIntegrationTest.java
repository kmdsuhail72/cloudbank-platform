package com.cloudbank.notification.inbox;

import com.cloudbank.notification.transfer.TransferNotification;
import com.cloudbank.notification.transfer.TransferNotificationRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class InboxPersistenceIntegrationTest {

    @Autowired
    private InboxEventRepository inboxRepository;

    @Autowired
    private TransferNotificationRepository
            notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAllInBatch();
        inboxRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        inboxRepository.deleteAllInBatch();
    }

    @Test
    void shouldPersistInboxAndTransferNotification() {
        UUID eventId =
                UUID.randomUUID();

        UUID transferId =
                UUID.randomUUID();

        Instant occurredAt =
                Instant.now()
                        .truncatedTo(
                                ChronoUnit.MICROS
                        );

        InboxEvent inbox =
                inboxRepository.saveAndFlush(
                        new InboxEvent(
                                eventId,
                                "TRANSFER_POSTED",
                                1,
                                "TRANSFER",
                                transferId,
                                occurredAt,
                                """
                                {"requestId":"%s"}
                                """.formatted(
                                        transferId
                                )
                        )
                );

        TransferNotification notification =
                notificationRepository.saveAndFlush(
                        new TransferNotification(
                                UUID.randomUUID(),
                                eventId,
                                transferId,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                new BigDecimal(
                                        "25.0000"
                                ),
                                "INR",
                                occurredAt
                        )
                );

        assertEquals(
                eventId,
                notification.getEventId()
        );

        assertEquals(
                1,
                notificationRepository
                        .countByEventId(
                                eventId
                        )
        );

        assertNotNull(
                inbox.getReceivedAt()
        );

        inbox.markProcessed(
                Instant.now()
                        .truncatedTo(
                                ChronoUnit.MICROS
                        )
        );

        inboxRepository.saveAndFlush(
                inbox
        );

        assertNotNull(
                inbox.getProcessedAt()
        );
    }

    @Test
    void shouldRejectDuplicateInboxEventId() {
        UUID eventId =
                UUID.randomUUID();

        UUID aggregateId =
                UUID.randomUUID();

        Instant occurredAt =
                Instant.now()
                        .truncatedTo(
                                ChronoUnit.MICROS
                        );

        inboxRepository.saveAndFlush(
                new InboxEvent(
                        eventId,
                        "TRANSFER_POSTED",
                        1,
                        "TRANSFER",
                        aggregateId,
                        occurredAt,
                        """
                        {"requestId":"%s"}
                        """.formatted(
                                aggregateId
                        )
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO inbox_events (
                            event_id,
                            event_type,
                            event_version,
                            aggregate_type,
                            aggregate_id,
                            occurred_at,
                            payload
                        ) VALUES (
                            ?,
                            'TRANSFER_POSTED',
                            1,
                            'TRANSFER',
                            ?,
                            ?,
                            ?
                        )
                        """,
                        eventId,
                        UUID.randomUUID(),
                        java.sql.Timestamp.from(
                                occurredAt
                        ),
                        "{}"
                )
        );

        assertEquals(
                1,
                inboxRepository.count()
        );
    }
}
