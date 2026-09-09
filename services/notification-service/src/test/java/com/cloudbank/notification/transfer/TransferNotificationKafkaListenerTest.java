package com.cloudbank.notification.transfer;

import org.junit.jupiter.api.Test;

import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferNotificationKafkaListenerTest {

    @Test
    void shouldAcknowledgeAfterProcessingReturns() {
        TransferNotificationProcessingService service =
                mock(
                        TransferNotificationProcessingService.class
                );

        Acknowledgment acknowledgment =
                mock(
                        Acknowledgment.class
                );

        when(
                service.process("{}")
        ).thenReturn(
                TransferNotificationProcessingService
                        .ProcessingOutcome.PROCESSED
        );

        TransferNotificationKafkaListener listener =
                new TransferNotificationKafkaListener(
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

        order.verify(service)
                .process("{}");

        order.verify(acknowledgment)
                .acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenProcessingFails() {
        TransferNotificationProcessingService service =
                mock(
                        TransferNotificationProcessingService.class
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

        TransferNotificationKafkaListener listener =
                new TransferNotificationKafkaListener(
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
