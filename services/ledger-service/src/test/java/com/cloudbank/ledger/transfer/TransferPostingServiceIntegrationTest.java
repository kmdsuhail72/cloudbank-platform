package com.cloudbank.ledger.transfer;

import com.cloudbank.ledger.outbox.OutboxEventRepository;
import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerBalanceService;
import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournalRepository;
import com.cloudbank.ledger.journal.LedgerJournalService;
import com.cloudbank.ledger.journal.LedgerPostingCommand;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransferPostingServiceIntegrationTest {

    private static final UUID ACTOR_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    @Autowired
    private TransferPostingService transferPostingService;

    @Autowired
    private LedgerJournalService journalService;

    @Autowired
    private LedgerBalanceService balanceService;

    @Autowired
    private LedgerJournalRepository journalRepository;

    @Autowired
    private LedgerPostingRepository postingRepository;

    @AfterEach
    void cleanDatabase() {
        postingRepository.deleteAll();
        postingRepository.flush();

        journalRepository.deleteAll();
        journalRepository.flush();
    }

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldPostTransferAndDeriveBothBalances() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        fund(
                sourceId,
                "100.0000"
        );

        TransferResult result =
                transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "40.0000"
                        ),
                        active(
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                );

        assertEquals(
                sourceId,
                result.sourceAccountId()
        );

        assertEquals(
                destinationId,
                result.destinationAccountId()
        );

        assertEquals(
                new BigDecimal(
                        "60.0000"
                ),
                balanceService
                        .getPostedBalance(
                                sourceId,
                                "INR"
                        )
                        .postedBalance()
        );

        assertEquals(
                new BigDecimal(
                        "40.0000"
                ),
                balanceService
                        .getPostedBalance(
                                destinationId,
                                "INR"
                        )
                        .postedBalance()
        );

        assertEquals(
                2,
                journalRepository.count()
        );

        assertEquals(
                4,
                postingRepository.count()
        );
    }

    @Test
    void shouldReturnOriginalJournalForIdenticalRetry() {
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
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                );

        TransferResult retry =
                transferPostingService.post(
                        command,
                        active(
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                );

        assertNotNull(
                first.createdAt()
        );

        assertEquals(
                first.createdAt(),
                retry.createdAt()
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
    }

    @Test
    void shouldRejectRequestIdReuseWithDifferentAmount() {
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

        transferPostingService.post(
                command(
                        requestId,
                        sourceId,
                        destinationId,
                        "25.0000"
                ),
                active(
                        sourceId,
                        "INR"
                ),
                active(
                        destinationId,
                        "INR"
                )
        );

        assertThrows(
                TransferIdempotencyConflictException.class,
                () -> transferPostingService.post(
                        command(
                                requestId,
                                sourceId,
                                destinationId,
                                "30.0000"
                        ),
                        active(
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                )
        );

        assertEquals(
                2,
                journalRepository.count()
        );

        assertEquals(
                4,
                postingRepository.count()
        );
    }

    @Test
    void shouldRejectInsufficientFundsWithoutTransferJournal() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        fund(
                sourceId,
                "10.0000"
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "20.0000"
                        ),
                        active(
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                )
        );

        assertEquals(
                1,
                journalRepository.count()
        );

        assertEquals(
                2,
                postingRepository.count()
        );
    }

    @Test
    void shouldRejectFrozenSource() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        assertThrows(
                AccountNotActiveForTransferException.class,
                () -> transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "10.0000"
                        ),
                        account(
                                sourceId,
                                "INR",
                                "FROZEN"
                        ),
                        active(
                                destinationId,
                                "INR"
                        )
                )
        );

        assertEquals(
                0,
                journalRepository.count()
        );
    }

    @Test
    void shouldRejectFrozenDestination() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        assertThrows(
                AccountNotActiveForTransferException.class,
                () -> transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "10.0000"
                        ),
                        active(
                                sourceId,
                                "INR"
                        ),
                        account(
                                destinationId,
                                "INR",
                                "FROZEN"
                        )
                )
        );

        assertEquals(
                0,
                journalRepository.count()
        );
    }

    @Test
    void shouldRejectCurrencyMismatch() {
        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        assertThrows(
                TransferCurrencyMismatchException.class,
                () -> transferPostingService.post(
                        command(
                                UUID.randomUUID(),
                                sourceId,
                                destinationId,
                                "10.0000"
                        ),
                        active(
                                sourceId,
                                "INR"
                        ),
                        active(
                                destinationId,
                                "USD"
                        )
                )
        );

        assertEquals(
                0,
                journalRepository.count()
        );
    }

    @Test
    void shouldPreventConcurrentDoubleSpend()
            throws Exception {

        UUID sourceId =
                UUID.randomUUID();

        UUID destinationOne =
                UUID.randomUUID();

        UUID destinationTwo =
                UUID.randomUUID();

        fund(
                sourceId,
                "100.0000"
        );

        CountDownLatch start =
                new CountDownLatch(
                        1
                );

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        2
                );

        try {
            Future<Boolean> first =
                    executor.submit(
                            () -> attemptTransfer(
                                    start,
                                    command(
                                            UUID.randomUUID(),
                                            sourceId,
                                            destinationOne,
                                            "80.0000"
                                    ),
                                    active(
                                            sourceId,
                                            "INR"
                                    ),
                                    active(
                                            destinationOne,
                                            "INR"
                                    )
                            )
                    );

            Future<Boolean> second =
                    executor.submit(
                            () -> attemptTransfer(
                                    start,
                                    command(
                                            UUID.randomUUID(),
                                            sourceId,
                                            destinationTwo,
                                            "80.0000"
                                    ),
                                    active(
                                            sourceId,
                                            "INR"
                                    ),
                                    active(
                                            destinationTwo,
                                            "INR"
                                    )
                            )
                    );

            start.countDown();

            boolean firstPosted =
                    first.get(
                            15,
                            TimeUnit.SECONDS
                    );

            boolean secondPosted =
                    second.get(
                            15,
                            TimeUnit.SECONDS
                    );

            assertNotEquals(
                    firstPosted,
                    secondPosted
            );

        } finally {
            executor.shutdownNow();
        }

        assertEquals(
                new BigDecimal(
                        "20.0000"
                ),
                balanceService
                        .getPostedBalance(
                                sourceId,
                                "INR"
                        )
                        .postedBalance()
        );

        assertEquals(
                2,
                journalRepository.count()
        );

        assertEquals(
                4,
                postingRepository.count()
        );
    }

    private boolean attemptTransfer(
            CountDownLatch start,
            TransferCommand command,
            AccountOwnershipResponse source,
            AccountOwnershipResponse destination
    ) throws InterruptedException {

        start.await();

        try {
            transferPostingService.post(
                    command,
                    source,
                    destination
            );

            return true;

        } catch (InsufficientFundsException exception) {

            return false;
        }
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

    private static AccountOwnershipResponse active(
            UUID accountId,
            String currency
    ) {
        return account(
                accountId,
                currency,
                "ACTIVE"
        );
    }

    private static AccountOwnershipResponse account(
            UUID accountId,
            String currency,
            String status
    ) {
        return new AccountOwnershipResponse(
                accountId,
                currency,
                status
        );
    }

    @AfterEach
    void cleanTransferOutboxEvents() {
        outboxEventRepository.deleteAllInBatch();
    }

}