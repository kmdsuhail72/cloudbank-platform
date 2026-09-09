package com.cloudbank.notification.transfer;

import com.cloudbank.notification.inbox.InboxEvent;
import com.cloudbank.notification.inbox.InboxEventRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = {
                "cloudbank.kafka.consumer.enabled=false"
        }
)
class TransferNotificationProcessingServiceIntegrationTest {

    @Autowired
    private TransferNotificationProcessingService processingService;

    @Autowired
    private InboxEventRepository inboxRepository;

    @Autowired
    private TransferNotificationRepository notificationRepository;

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
    void shouldProcessDuplicateEventExactlyOnce() {
        Fixture fixture =
                fixture();

        assertEquals(
                TransferNotificationProcessingService
                        .ProcessingOutcome.PROCESSED,
                processingService.process(
                        fixture.message()
                )
        );

        assertEquals(
                TransferNotificationProcessingService
                        .ProcessingOutcome.DUPLICATE,
                processingService.process(
                        fixture.message()
                )
        );

        assertEquals(
                1,
                inboxRepository.count()
        );

        assertEquals(
                1,
                notificationRepository.count()
        );

        InboxEvent inbox =
                inboxRepository
                        .findById(
                                fixture.eventId()
                        )
                        .orElseThrow();

        assertNotNull(
                inbox.getProcessedAt()
        );

        assertEquals(
                1,
                notificationRepository.countByEventId(
                        fixture.eventId()
                )
        );
    }

    @Test
    void shouldRejectInvalidEventWithoutPersistingAnything() {
        Fixture fixture =
                fixture();

        String invalid =
                fixture.message().replace(
                        "\"TRANSFER_POSTED\"",
                        "\"UNKNOWN_EVENT\""
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> processingService.process(
                        invalid
                )
        );

        assertEquals(
                0,
                inboxRepository.count()
        );

        assertEquals(
                0,
                notificationRepository.count()
        );
    }

    private static Fixture fixture() {
        UUID eventId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        String message =
                """
                {
                  "eventId":"%s",
                  "eventType":"TRANSFER_POSTED",
                  "eventVersion":1,
                  "aggregateType":"TRANSFER",
                  "aggregateId":"%s",
                  "occurredAt":"2026-09-10T00:00:00Z",
                  "data":{
                    "requestId":"%s",
                    "journalId":"%s",
                    "sourceAccountId":"%s",
                    "destinationAccountId":"%s",
                    "amount":25.0000,
                    "currency":"INR",
                    "postedAt":"2026-09-10T00:00:00Z"
                  }
                }
                """.formatted(
                        eventId,
                        requestId,
                        requestId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        return new Fixture(
                eventId,
                message
        );
    }

    private record Fixture(
            UUID eventId,
            String message
    ) {
    }
}
