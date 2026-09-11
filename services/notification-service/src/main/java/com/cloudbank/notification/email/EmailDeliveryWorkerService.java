package com.cloudbank.notification.email;

import com.cloudbank.notification.contact.RecipientContact;
import com.cloudbank.notification.contact.RecipientContactRepository;
import com.cloudbank.notification.transfer.TransferNotification;
import com.cloudbank.notification.transfer.TransferNotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@ConditionalOnProperty(
        prefix = "cloudbank.email.worker",
        name = "enabled",
        havingValue = "true"
)
public class EmailDeliveryWorkerService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailDeliveryWorkerService.class
            );

    public enum ProcessingOutcome {
        NO_WORK,
        SENT,
        RETRY_SCHEDULED,
        FAILED,
        LOST_CLAIM
    }

    private static final int MAX_ERROR_LENGTH =
            2000;

    private final EmailDeliveryClaimService
            claimService;

    private final EmailDeliveryStateService
            stateService;

    private final EmailDeliveryRetryPolicy
            retryPolicy;

    private final RecipientContactRepository
            contactRepository;

    private final TransferNotificationRepository
            notificationRepository;

    private final EmailDeliveryTransport
            transport;

    private final int maxAttempts;

    public EmailDeliveryWorkerService(
            EmailDeliveryClaimService claimService,
            EmailDeliveryStateService stateService,
            EmailDeliveryRetryPolicy retryPolicy,
            RecipientContactRepository contactRepository,
            TransferNotificationRepository notificationRepository,
            EmailDeliveryTransport transport,
            @Value("${cloudbank.email.worker.max-attempts:5}")
            int maxAttempts
    ) {
        this.claimService =
                Objects.requireNonNull(
                        claimService
                );

        this.stateService =
                Objects.requireNonNull(
                        stateService
                );

        this.retryPolicy =
                Objects.requireNonNull(
                        retryPolicy
                );

        this.contactRepository =
                Objects.requireNonNull(
                        contactRepository
                );

        this.notificationRepository =
                Objects.requireNonNull(
                        notificationRepository
                );

        this.transport =
                Objects.requireNonNull(
                        transport
                );

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Max email delivery attempts must be positive"
            );
        }

        this.maxAttempts =
                maxAttempts;
    }

    /*
     * Intentionally NOT transactional.
     *
     * claimNext() opens and commits its own short transaction.
     * The transport call therefore occurs after the claim transaction
     * has completed.
     *
     * markSent()/releaseForRetry()/markFailed() each open their own
     * short fenced transaction afterward.
     */
    public ProcessingOutcome processNext() {
        return processNext(
                Instant.now()
        );
    }

    ProcessingOutcome processNext(
            Instant now
    ) {
        Objects.requireNonNull(
                now,
                "Worker time is required"
        );

        Optional<EmailDeliveryClaim> optionalClaim =
                claimService.claimNext(
                        now,
                        EmailDeliveryClaimService.DEFAULT_LEASE_DURATION
                );

        if (optionalClaim.isEmpty()) {
            return ProcessingOutcome.NO_WORK;
        }

        EmailDeliveryClaim claim =
                optionalClaim.orElseThrow();

        TransferNotification notification =
                notificationRepository.findById(
                        claim.notificationId()
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "Transfer notification not found"
                        )
                );

        Optional<RecipientContact> contact =
                contactRepository.findById(
                        claim.recipientUserId()
                );

        if (contact.isEmpty()) {
            LOGGER.warn(
                    "email_delivery_recipient_contact_missing deliveryId={} attempt={}",
                    claim.deliveryId(),
                    claim.attemptCount()
            );

            return handleFailure(
                    claim,
                    now,
                    "Recipient contact not available"
            );
        }

        EmailDeliveryMessage message =
                toMessage(
                        claim,
                        notification,
                        contact.orElseThrow()
                );

        try {
            transport.send(
                    message
            );
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "email_delivery_transport_failure deliveryId={} attempt={} exceptionType={}",
                    claim.deliveryId(),
                    claim.attemptCount(),
                    exception.getClass()
                            .getSimpleName()
            );

            return handleFailure(
                    claim,
                    now,
                    describeFailure(
                            exception
                    )
            );
        }

        boolean markedSent =
                stateService.markSent(
                        claim.deliveryId(),
                        claim.claimToken(),
                        Instant.now()
                );

        if (!markedSent) {
            LOGGER.warn(
                    "email_delivery_sent_lost_claim deliveryId={} attempt={}",
                    claim.deliveryId(),
                    claim.attemptCount()
            );
        }

        return markedSent
                ? ProcessingOutcome.SENT
                : ProcessingOutcome.LOST_CLAIM;
    }

    private ProcessingOutcome handleFailure(
            EmailDeliveryClaim claim,
            Instant now,
            String error
    ) {
        if (claim.attemptCount() >= maxAttempts) {
            boolean failed =
                    stateService.markFailed(
                            claim.deliveryId(),
                            claim.claimToken(),
                            now,
                            error
                    );

            if (failed) {
                LOGGER.error(
                        "email_delivery_terminal_failed deliveryId={} attempt={}",
                        claim.deliveryId(),
                        claim.attemptCount()
                );
            } else {
                LOGGER.warn(
                        "email_delivery_terminal_failure_lost_claim deliveryId={} attempt={}",
                        claim.deliveryId(),
                        claim.attemptCount()
                );
            }

            return failed
                    ? ProcessingOutcome.FAILED
                    : ProcessingOutcome.LOST_CLAIM;
        }

        Instant nextAttemptAt =
                retryPolicy.nextAttemptAt(
                        now,
                        claim.attemptCount()
                );

        boolean released =
                stateService.releaseForRetry(
                        claim.deliveryId(),
                        claim.claimToken(),
                        now,
                        nextAttemptAt,
                        error
                );

        if (released) {
            LOGGER.info(
                    "email_delivery_retry_scheduled deliveryId={} attempt={} nextAttemptAt={}",
                    claim.deliveryId(),
                    claim.attemptCount(),
                    nextAttemptAt
            );
        } else {
            LOGGER.warn(
                    "email_delivery_retry_lost_claim deliveryId={} attempt={}",
                    claim.deliveryId(),
                    claim.attemptCount()
            );
        }

        return released
                ? ProcessingOutcome.RETRY_SCHEDULED
                : ProcessingOutcome.LOST_CLAIM;
    }

    private static EmailDeliveryMessage toMessage(
            EmailDeliveryClaim claim,
            TransferNotification notification,
            RecipientContact contact
    ) {
        if (!notification.getId()
                .equals(claim.notificationId())) {
            throw new IllegalStateException(
                    "Claim notification mismatch"
            );
        }

        if (!contact.getUserId()
                .equals(claim.recipientUserId())) {
            throw new IllegalStateException(
                    "Claim recipient mismatch"
            );
        }

        return new EmailDeliveryMessage(
                claim.deliveryId(),
                claim.notificationId(),
                claim.recipientUserId(),
                contact.getEmail(),
                notification.getTransferRequestId(),
                notification.getJournalId(),
                notification.getSourceAccountId(),
                notification.getDestinationAccountId(),
                notification.getAmount(),
                notification.getCurrency(),
                notification.getPostedAt()
        );
    }

    private static String describeFailure(
            RuntimeException exception
    ) {
        String message =
                exception.getMessage();

        String value =
                exception.getClass()
                        .getSimpleName();

        if (message != null
                && !message.isBlank()) {
            value =
                    value
                            + ": "
                            + message;
        }

        if (value.length()
                > MAX_ERROR_LENGTH) {
            return value.substring(
                    0,
                    MAX_ERROR_LENGTH
            );
        }

        return value;
    }
}
