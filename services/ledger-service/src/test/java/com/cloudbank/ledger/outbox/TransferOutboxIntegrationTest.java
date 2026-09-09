package com.cloudbank.ledger.outbox;

import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournalRepository;
import com.cloudbank.ledger.journal.LedgerJournalService;
import com.cloudbank.ledger.journal.LedgerPostingCommand;
import com.cloudbank.ledger.journal.LedgerPostingRepository;
import com.cloudbank.ledger.transfer.InsufficientFundsException;
import com.cloudbank.ledger.transfer.TransferCommand;
import com.cloudbank.ledger.transfer.TransferPostingService;
import com.cloudbank.ledger.transfer.TransferResult;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransferOutboxIntegrationTest {

    @Autowired
    private TransferPostingService transferPostingService;

    @Autowired
    private LedgerJournalService journalService;

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldPersistTransferAndOutboxAtomically() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        fund(
                sourceId,
                "100.0000"
        );

        TransferResult result =
                transferPostingService.post(
                        command(
                                requestId,
                                sourceId,
                                destinationId,
                                "25.0000"
                        ),
                        active(
                                sourceId
                        ),
                        active(
                                destinationId
                        )
                );

        OutboxEvent event =
                outboxRepository
                        .findByAggregateTypeAndAggregateIdAndEventType(
                                TransferOutboxService.AGGREGATE_TYPE,
                                requestId,
                                TransferOutboxService.EVENT_TYPE
                        )
                        .orElseThrow();

        assertEquals(
                2,
                journalRepository.count()
        );

        assertEquals(
                4,
                postingRepository.count()
        );

        assertEquals(
                1,
                outboxRepository.count()
        );

        assertEquals(
                requestId,
                event.getAggregateId()
        );

        assertEquals(
                TransferOutboxService.EVENT_VERSION,
                event.getEventVersion()
        );

        assertEquals(
                expectedPayload(
                        result
                ),
                event.getPayload()
        );
    }

    @Test
    void shouldNotCreateJournalOrOutboxForFailedTransfer() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        assertThrows(
                InsufficientFundsException.class,
                () -> transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "25.0000"
                        ),
                        active(
                                sourceId
                        ),
                        active(
                                destinationId
                        )
                )
        );

        assertEquals(
                0,
                journalRepository.count()
        );

        assertEquals(
                0,
                postingRepository.count()
        );

        assertEquals(
                0,
                outboxRepository.count()
        );
    }

    @Test
    void shouldKeepExactlyOneOutboxEventForIdenticalRetry() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        fund(
                sourceId,
                "100.0000"
        );

        TransferCommand command =
                command(
                        requestId,
                        sourceId,
                        destinationId,
                        "25.0000"
                );

        TransferResult first =
                transferPostingService.post(
                        command,
                        active(
                                sourceId
                        ),
                        active(
                                destinationId
                        )
                );

        TransferResult retry =
                transferPostingService.post(
                        command,
                        active(
                                sourceId
                        ),
                        active(
                                destinationId
                        )
                );

        assertEquals(
                first,
                retry
        );

        assertEquals(
                2,
                journalRepository.count()
        );

        assertEquals(
                4,
                postingRepository.count()
        );

        assertEquals(
                1,
                outboxRepository.count()
        );

        assertEquals(
                1,
                outboxRepository.countByPublishedAtIsNull()
        );
    }

    @Test
    void shouldRollbackTransferWhenOutboxPersistenceFails() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        fund(
                sourceId,
                "100.0000"
        );

        /*
         * Deliberately occupy the unique outbox key first.
         * The later transfer reaches journal/posting persistence,
         * then its outbox write fails. The outer transaction must
         * roll the transfer journal/postings back.
         */
        outboxRepository.saveAndFlush(
                new OutboxEvent(
                        TransferOutboxService.AGGREGATE_TYPE,
                        requestId,
                        TransferOutboxService.EVENT_TYPE,
                        TransferOutboxService.EVENT_VERSION,
                        "{\"preexisting\":true}"
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> transferPostingService.post(
                        command(
                                requestId,
                                sourceId,
                                destinationId,
                                "25.0000"
                        ),
                        active(
                                sourceId
                        ),
                        active(
                                destinationId
                        )
                )
        );

        assertTrue(
                journalRepository
                        .findByReferenceTypeAndReferenceId(
                                "TRANSFER",
                                requestId
                        )
                        .isEmpty()
        );

        /*
         * Only the funding journal and its two postings survive.
         */
        assertEquals(
                1,
                journalRepository.count()
        );

        assertEquals(
                2,
                postingRepository.count()
        );

        /*
         * Only the deliberately pre-existing event survives.
         */
        assertEquals(
                1,
                outboxRepository.count()
        );
    }

    private void fund(
            UUID accountId,
            String amount
    ) {
        BigDecimal value =
                new BigDecimal(
                        amount
                );

        journalService.postJournal(
                "TEST_FUNDING",
                UUID.randomUUID(),
                List.of(
                        new LedgerPostingCommand(
                                UUID.randomUUID(),
                                LedgerEntryType.DEBIT,
                                value,
                                "INR"
                        ),
                        new LedgerPostingCommand(
                                accountId,
                                LedgerEntryType.CREDIT,
                                value,
                                "INR"
                        )
                )
        );
    }

    private static AccountOwnershipResponse active(
            UUID accountId
    ) {
        return new AccountOwnershipResponse(
                accountId,
                "INR",
                "ACTIVE"
        );
    }

    private static TransferCommand command(
            UUID requestId,
            UUID sourceId,
            UUID destinationId,
            String amount
    ) {
        return new TransferCommand(
                requestId,
                sourceId,
                destinationId,
                new BigDecimal(
                        amount
                )
        );
    }

    private static String expectedPayload(
            TransferResult transfer
    ) {
        return """
                {"requestId":"%s","journalId":"%s","sourceAccountId":"%s","destinationAccountId":"%s","amount":%s,"currency":"%s","postedAt":"%s"}                """.formatted(
                        transfer.requestId(),
                        transfer.journalId(),
                        transfer.sourceAccountId(),
                        transfer.destinationAccountId(),
                        transfer.amount().toPlainString(),
                        transfer.currency(),
                        transfer.createdAt()
                );
    }

    private void cleanDatabase() {
        outboxRepository.deleteAllInBatch();
        postingRepository.deleteAllInBatch();
        journalRepository.deleteAllInBatch();
    }
}
