package com.cloudbank.auth.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.kafka.core.KafkaTemplate;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthOutboxPublishingServiceTest {

    private AuthOutboxEventRepository repository;

    private KafkaTemplate<String, String>
            kafkaTemplate;

    private AuthOutboxPublishingService service;

    @BeforeEach
    void setUp() {
        repository =
                mock(
                        AuthOutboxEventRepository.class
                );

        kafkaTemplate =
                mock(
                        KafkaTemplate.class
                );

        ObjectMapper objectMapper =
                JsonMapper.builder()
                        .findAndAddModules()
                        .build();

        service =
                new AuthOutboxPublishingService(
                        repository,
                        kafkaTemplate,
                        objectMapper,
                        "cloudbank.user-contact.v1",
                        5
                );
    }

    @Test
    void shouldPublishAndMarkEventPublished() {
        UUID userId =
                UUID.randomUUID();

        AuthOutboxEvent event =
                event(
                        userId
                );

        when(
                repository.claimNextPending()
        ).thenReturn(
                Optional.of(
                        event
                )
        );

        when(
                kafkaTemplate.send(
                        eq(
                                "cloudbank.user-contact.v1"
                        ),
                        eq(
                                userId.toString()
                        ),
                        anyString()
                )
        ).thenReturn(
                CompletableFuture.completedFuture(
                        null
                )
        );

        assertEquals(
                AuthOutboxPublishingService
                        .PublishOutcome.PUBLISHED,
                service.publishNextPending()
        );

        assertNotNull(
                event.getPublishedAt()
        );

        assertEquals(
                0,
                event.getAttemptCount()
        );

        assertNull(
                event.getLastError()
        );

        verify(
                repository
        ).saveAndFlush(
                event
        );
    }

    @Test
    void shouldRecordFailureWithoutMarkingPublished() {
        UUID userId =
                UUID.randomUUID();

        AuthOutboxEvent event =
                event(
                        userId
                );

        when(
                repository.claimNextPending()
        ).thenReturn(
                Optional.of(
                        event
                )
        );

        CompletableFuture future =
                new CompletableFuture();

        future.completeExceptionally(
                new IllegalStateException(
                        "broker unavailable"
                )
        );

        when(
                kafkaTemplate.send(
                        eq(
                                "cloudbank.user-contact.v1"
                        ),
                        eq(
                                userId.toString()
                        ),
                        anyString()
                )
        ).thenReturn(
                future
        );

        assertEquals(
                AuthOutboxPublishingService
                        .PublishOutcome.FAILED,
                service.publishNextPending()
        );

        assertNull(
                event.getPublishedAt()
        );

        assertEquals(
                1,
                event.getAttemptCount()
        );

        assertNotNull(
                event.getLastError()
        );
    }

    @Test
    void shouldReturnNoneWhenQueueEmpty() {
        when(
                repository.claimNextPending()
        ).thenReturn(
                Optional.empty()
        );

        assertEquals(
                AuthOutboxPublishingService
                        .PublishOutcome.NONE,
                service.publishNextPending()
        );

        verify(
                kafkaTemplate,
                never()
        ).send(
                anyString(),
                anyString(),
                anyString()
        );
    }

    private static AuthOutboxEvent event(
            UUID userId
    ) {
        return new AuthOutboxEvent(
                UserContactOutboxService.AGGREGATE_TYPE,
                userId,
                UserContactOutboxService.EVENT_TYPE,
                UserContactOutboxService.EVENT_VERSION,
                """
                {"userId":"%s","email":"user@example.com"}
                """.formatted(
                        userId
                )
        );
    }
}
