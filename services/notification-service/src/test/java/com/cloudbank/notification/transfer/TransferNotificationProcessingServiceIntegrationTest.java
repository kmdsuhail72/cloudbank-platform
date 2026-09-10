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
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void shouldProcessDuplicateVersionOneEventExactlyOnce() {
        Fixture fixture =
                fixture(
                        1,
                        null
                );

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

        assertNull(
                notificationRepository
                        .findAll()
                        .getFirst()
                        .getActorUserId()
        );
    }

    @Test
    void shouldPersistActorUserIdForVersionTwo() {
        UUID actorUserId =
                UUID.randomUUID();

        Fixture fixture =
                fixture(
                        2,
                        actorUserId
                );

        assertEquals(
                TransferNotificationProcessingService
                        .ProcessingOutcome.PROCESSED,
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

        assertEquals(
                actorUserId,
                notificationRepository
                        .findAll()
                        .getFirst()
                        .getActorUserId()
        );
    }

    @Test
    void shouldRejectVersionTwoWithoutActorUserId() {
        Fixture fixture =
                fixture(
                        2,
                        null
                );

        assertThrows(
                NullPointerException.class,
                () -> processingService.process(
                        fixture.message()
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

    @Test
    void shouldRejectInvalidEventWithoutPersistingAnything() {
        Fixture fixture =
                fixture(
                        1,
                        null
                );

        String invalid =
                fixture.message()
                        .replace(
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

    private static Fixture fixture(
            int eventVersion,
            UUID actorUserId
    ) {
        UUID eventId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        String actorField =
                actorUserId == null
                        ? ""
                        : "\"actorUserId\":\""
                        + actorUserId
                        + "\",";

        String message =
                """
                {
                  "eventId":"%s",
                  "eventType":"TRANSFER_POSTED",
                  "eventVersion":%d,
                  "aggregateType":"TRANSFER",
                  "aggregateId":"%s",
                  "occurredAt":"2026-09-10T00:00:00Z",
                  "data":{
                    "requestId":"%s",
                    %s
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
                        eventVersion,
                        requestId,
                        requestId,
                        actorField,
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
