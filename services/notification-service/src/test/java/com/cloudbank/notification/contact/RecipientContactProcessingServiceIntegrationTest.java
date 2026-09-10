package com.cloudbank.notification.contact;

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
                "cloudbank.kafka.consumer.enabled=false",
                "cloudbank.kafka.contact-consumer.enabled=false"
        }
)
class RecipientContactProcessingServiceIntegrationTest {

    @Autowired
    private RecipientContactProcessingService
            processingService;

    @Autowired
    private RecipientContactRepository
            contactRepository;

    @Autowired
    private InboxEventRepository
            inboxRepository;

    @BeforeEach
    void setUp() {
        clean();
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    @Test
    void shouldProcessDuplicateContactEventExactlyOnce() {
        Fixture fixture =
                fixture();

        assertEquals(
                RecipientContactProcessingService
                        .ProcessingOutcome.PROCESSED,
                processingService.process(
                        fixture.message()
                )
        );

        assertEquals(
                RecipientContactProcessingService
                        .ProcessingOutcome.DUPLICATE,
                processingService.process(
                        fixture.message()
                )
        );

        assertEquals(
                1,
                contactRepository.count()
        );

        RecipientContact contact =
                contactRepository
                        .findById(
                                fixture.userId()
                        )
                        .orElseThrow();

        assertEquals(
                fixture.email(),
                contact.getEmail()
        );

        assertEquals(
                fixture.eventId(),
                contact.getSourceEventId()
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
    }

    @Test
    void shouldRejectAggregateUserMismatchWithoutPersisting() {
        Fixture fixture =
                fixture();

        String invalid =
                fixture.message()
                        .replace(
                                "\"aggregateId\":\""
                                        + fixture.userId()
                                        + "\"",
                                "\"aggregateId\":\""
                                        + UUID.randomUUID()
                                        + "\""
                        );

        assertThrows(
                IllegalArgumentException.class,
                () -> processingService.process(
                        invalid
                )
        );

        assertEquals(
                0,
                contactRepository.count()
        );

        assertEquals(
                0,
                inboxRepository.count()
        );
    }

    private static Fixture fixture() {
        UUID eventId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        String email =
                "contact-test@cloudbank.local";

        String message =
                """
                {
                  "eventId":"%s",
                  "eventType":"USER_CONTACT_REGISTERED",
                  "eventVersion":1,
                  "aggregateType":"AUTH_USER",
                  "aggregateId":"%s",
                  "occurredAt":"2026-09-10T00:00:00Z",
                  "data":{
                    "userId":"%s",
                    "email":"%s"
                  }
                }
                """.formatted(
                        eventId,
                        userId,
                        userId,
                        email
                );

        return new Fixture(
                eventId,
                userId,
                email,
                message
        );
    }

    private void clean() {
        contactRepository.deleteAllInBatch();
        inboxRepository.deleteAllInBatch();
    }

    private record Fixture(
            UUID eventId,
            UUID userId,
            String email,
            String message
    ) {
    }
}
