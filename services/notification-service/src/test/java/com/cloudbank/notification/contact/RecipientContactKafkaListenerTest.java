package com.cloudbank.notification.contact;

import org.junit.jupiter.api.Test;

import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecipientContactKafkaListenerTest {

    @Test
    void shouldAcknowledgeAfterProcessingReturns() {
        RecipientContactProcessingService service =
                mock(
                        RecipientContactProcessingService.class
                );

        Acknowledgment acknowledgment =
                mock(
                        Acknowledgment.class
                );

        when(
                service.process("{}")
        ).thenReturn(
                RecipientContactProcessingService
                        .ProcessingOutcome.PROCESSED
        );

        RecipientContactKafkaListener listener =
                new RecipientContactKafkaListener(
                        service
                );

        listener.consume(
                "{}",
                acknowledgment
        );

        var order =
                inOrder(
                        service,
                        acknowledgment
                );

        order.verify(
                service
        ).process(
                "{}"
        );

        order.verify(
                acknowledgment
        ).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenProcessingFails() {
        RecipientContactProcessingService service =
                mock(
                        RecipientContactProcessingService.class
                );

        Acknowledgment acknowledgment =
                mock(
                        Acknowledgment.class
                );

        when(
                service.process("{}")
        ).thenThrow(
                new IllegalStateException(
                        "database failed"
                )
        );

        RecipientContactKafkaListener listener =
                new RecipientContactKafkaListener(
                        service
                );

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        "{}",
                        acknowledgment
                )
        );

        verify(
                acknowledgment,
                never()
        ).acknowledge();
    }
}
