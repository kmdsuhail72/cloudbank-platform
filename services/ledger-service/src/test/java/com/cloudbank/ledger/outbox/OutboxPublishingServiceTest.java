package com.cloudbank.ledger.outbox;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublishingServiceTest {

    private OutboxEventRepository repository;

    private KafkaTemplate<String, String>
            kafkaTemplate;

    private ObjectMapper objectMapper;

    private OutboxPublishingService service;

    @BeforeEach
    void setUp() {
        repository =
                mock(
                        OutboxEventRepository.class
                );

        kafkaTemplate =
                mock(
                        KafkaTemplate.class
                );

        objectMapper =
                JsonMapper.builder()
                        .findAndAddModules()
                        .build();

        service =
                new OutboxPublishingService(
                        repository,
                        kafkaTemplate,
                        objectMapper,
                        "cloudbank.transfer.v1",
                        5
                );
    }

    @Test
    void shouldPublishAndMarkEventPublished()
            throws Exception {

        UUID requestId =
                UUID.randomUUID();

        OutboxEvent event =
                event(
                        requestId
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
                                "cloudbank.transfer.v1"
                        ),
                        eq(
                                requestId.toString()
                        ),
                        anyString()
                )
        ).thenReturn(
                CompletableFuture.completedFuture(
                        null
                )
        );

        OutboxPublishingService.PublishOutcome
                outcome =
                service.publishNextPending();

        assertEquals(
                OutboxPublishingService
                        .PublishOutcome
                        .PUBLISHED,
                outcome
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
        UUID requestId =
                UUID.randomUUID();

        OutboxEvent event =
                event(
                        requestId
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
                                "cloudbank.transfer.v1"
                        ),
                        eq(
                                requestId.toString()
                        ),
                        anyString()
                )
        ).thenReturn(
                future
        );

        OutboxPublishingService.PublishOutcome
                outcome =
                service.publishNextPending();

        assertEquals(
                OutboxPublishingService
                        .PublishOutcome
                        .FAILED,
                outcome
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

        assertTrue(
                event
                        .getLastError()
                        .contains(
                                "broker unavailable"
                        )
        );

        verify(
                repository
        ).saveAndFlush(
                event
        );
    }

    @Test
    void shouldReturnNoneWhenQueueIsEmpty() {
        when(
                repository.claimNextPending()
        ).thenReturn(
                Optional.empty()
        );

        assertEquals(
                OutboxPublishingService
                        .PublishOutcome
                        .NONE,
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

    private static OutboxEvent event(
            UUID requestId
    ) {
        return new OutboxEvent(
                TransferOutboxService
                        .AGGREGATE_TYPE,
                requestId,
                TransferOutboxService
                        .EVENT_TYPE,
                TransferOutboxService
                        .EVENT_VERSION,
                """
                {
                  "requestId":"%s",
                  "amount":25.0000,
                  "currency":"INR"
                }
                """.formatted(
                        requestId
                )
        );
    }
}
