package com.cloudbank.ledger.outbox;

import com.cloudbank.ledger.transfer.TransferResult;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class TransferOutboxService {

    public static final String AGGREGATE_TYPE =
            "TRANSFER";

    public static final String EVENT_TYPE =
            "TRANSFER_POSTED";

    public static final int EVENT_VERSION =
            2;

    private final OutboxEventRepository repository;

    public TransferOutboxService(
            OutboxEventRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository
                );
    }

    public OutboxEvent recordTransferPosted(
            TransferResult transfer,
            UUID actorUserId
    ) {
        Objects.requireNonNull(
                transfer,
                "Transfer result is required"
        );

        Objects.requireNonNull(
                actorUserId,
                "Actor user ID is required"
        );

        OutboxEvent event =
                new OutboxEvent(
                        AGGREGATE_TYPE,
                        transfer.requestId(),
                        EVENT_TYPE,
                        EVENT_VERSION,
                        payload(
                                transfer,
                                actorUserId
                        )
                );

        return repository.saveAndFlush(
                event
        );
    }

    private static String payload(
            TransferResult transfer,
            UUID actorUserId
    ) {
        return """
                {"requestId":"%s","actorUserId":"%s","journalId":"%s","sourceAccountId":"%s","destinationAccountId":"%s","amount":%s,"currency":"%s","postedAt":"%s"}\
                """.formatted(
                        transfer.requestId(),
                        actorUserId,
                        transfer.journalId(),
                        transfer.sourceAccountId(),
                        transfer.destinationAccountId(),
                        transfer.amount().toPlainString(),
                        transfer.currency(),
                        transfer.createdAt()
                );
    }
}
