package com.cloudbank.notification.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(
        prefix = "cloudbank.email.smtp",
        name = "enabled",
        havingValue = "true"
)
public class SmtpEmailDeliveryTransport
        implements EmailDeliveryTransport {

    private final JavaMailSender mailSender;

    private final String fromAddress;

    public SmtpEmailDeliveryTransport(
            JavaMailSender mailSender,
            @Value("${cloudbank.email.from}")
            String fromAddress
    ) {
        this.mailSender =
                Objects.requireNonNull(
                        mailSender
                );

        if (fromAddress == null
                || fromAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "Email from address is required"
            );
        }

        this.fromAddress =
                fromAddress;
    }

    @Override
    public void send(
            EmailDeliveryMessage message
    ) {
        Objects.requireNonNull(
                message,
                "Email delivery message is required"
        );

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setFrom(
                fromAddress
        );

        mail.setTo(
                message.recipientEmail()
        );

        mail.setSubject(
                "CloudBank transfer posted"
        );

        mail.setText(
                body(
                        message
                )
        );

        mailSender.send(
                mail
        );
    }

    private static String body(
            EmailDeliveryMessage message
    ) {
        return """
                Your CloudBank transfer has been posted successfully.

                Transfer request: %s
                Journal: %s
                Source account: %s
                Destination account: %s
                Amount: %s %s
                Posted at: %s
                """.formatted(
                message.transferRequestId(),
                message.journalId(),
                message.sourceAccountId(),
                message.destinationAccountId(),
                message.amount().toPlainString(),
                message.currency(),
                message.postedAt()
        );
    }
}
