package com.cloudbank.notification.email;

import org.junit.jupiter.api.Test;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailDeliveryTransportTest {

    @Test
    void shouldSendTransferEmailThroughMailSender() {
        JavaMailSender sender =
                mock(
                        JavaMailSender.class
                );

        SmtpEmailDeliveryTransport transport =
                new SmtpEmailDeliveryTransport(
                        sender,
                        "no-reply@cloudbank.local"
                );

        EmailDeliveryMessage message =
                new EmailDeliveryMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "recipient@example.com",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal(
                                "25.5000"
                        ),
                        "USD",
                        Instant.parse(
                                "2026-09-11T16:00:00Z"
                        )
                );

        transport.send(
                message
        );

        verify(
                sender
        ).send(
                any(
                        SimpleMailMessage.class
                )
        );
    }
}
