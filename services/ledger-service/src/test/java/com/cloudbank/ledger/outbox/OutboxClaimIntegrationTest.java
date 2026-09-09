package com.cloudbank.ledger.outbox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        properties = {
                "cloudbank.outbox.publisher.enabled=false"
        }
)
class OutboxClaimIntegrationTest {

    @Autowired
    private OutboxEventRepository repository;

    @Autowired
    private PlatformTransactionManager
            transactionManager;

    @BeforeEach
    void setUp() {
        repository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAllInBatch();
    }

    @Test
    void shouldSkipRowLockedByAnotherTransaction()
            throws Exception {

        OutboxEvent first =
                repository.saveAndFlush(
                        event()
                );

        Thread.sleep(
                5
        );

        OutboxEvent second =
                repository.saveAndFlush(
                        event()
                );

        TransactionTemplate transactions =
                new TransactionTemplate(
                        transactionManager
                );

        ExecutorService executor =
                Executors.newSingleThreadExecutor();

        CountDownLatch firstLocked =
                new CountDownLatch(
                        1
                );

        CountDownLatch releaseFirst =
                new CountDownLatch(
                        1
                );

        AtomicReference<UUID> firstClaim =
                new AtomicReference<>();

        try {
            Future<?> worker =
                    executor.submit(
                            () -> transactions.executeWithoutResult(
                                    status -> {
                                        OutboxEvent claimed =
                                                repository
                                                        .claimNextPending()
                                                        .orElseThrow();

                                        firstClaim.set(
                                                claimed.getId()
                                        );

                                        firstLocked.countDown();

                                        try {
                                            if (!releaseFirst.await(
                                                    5,
                                                    TimeUnit.SECONDS
                                            )) {
                                                throw new AssertionError(
                                                        "Timed out waiting to release first lock"
                                                );
                                            }
                                        } catch (
                                                InterruptedException exception
                                        ) {
                                            Thread
                                                    .currentThread()
                                                    .interrupt();

                                            throw new AssertionError(
                                                    exception
                                            );
                                        }
                                    }
                            )
                    );

            assertTrue(
                    firstLocked.await(
                            5,
                            TimeUnit.SECONDS
                    )
            );

            UUID secondClaim =
                    transactions.execute(
                            status -> repository
                                    .claimNextPending()
                                    .orElseThrow()
                                    .getId()
                    );

            assertEquals(
                    first.getId(),
                    firstClaim.get()
            );

            assertEquals(
                    second.getId(),
                    secondClaim
            );

            assertNotEquals(
                    firstClaim.get(),
                    secondClaim
            );

            releaseFirst.countDown();

            worker.get(
                    5,
                    TimeUnit.SECONDS
            );

        } finally {
            releaseFirst.countDown();

            executor.shutdownNow();
        }
    }

    private static OutboxEvent event() {
        UUID aggregateId =
                UUID.randomUUID();

        return new OutboxEvent(
                "TRANSFER",
                aggregateId,
                "TRANSFER_POSTED",
                1,
                """
                {"requestId":"%s"}
                """.formatted(
                        aggregateId
                )
        );
    }
}
