package com.cloudbank.ledger.transfer;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferRequestTest {

    @Test
    void shouldCreateTransferCommand() {
        UUID requestId =
                UUID.randomUUID();

        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        TransferCommand command =
                new TransferRequest(
                        requestId,
                        sourceId,
                        destinationId,
                        new BigDecimal(
                                "25.0000"
                        )
                ).toCommand();

        assertEquals(
                requestId,
                command.requestId()
        );

        assertEquals(
                sourceId,
                command.sourceAccountId()
        );

        assertEquals(
                destinationId,
                command.destinationAccountId()
        );

        assertEquals(
                new BigDecimal(
                        "25.0000"
                ),
                command.amount()
        );
    }

    @Test
    void shouldRejectMissingRequestId() {
        assertThrows(
                InvalidTransferException.class,
                () -> new TransferRequest(
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal(
                                "1.0000"
                        )
                ).toCommand()
        );
    }

    @Test
    void shouldRejectMissingSourceAccountId() {
        assertThrows(
                InvalidTransferException.class,
                () -> new TransferRequest(
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        new BigDecimal(
                                "1.0000"
                        )
                ).toCommand()
        );
    }

    @Test
    void shouldRejectMissingDestinationAccountId() {
        assertThrows(
                InvalidTransferException.class,
                () -> new TransferRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        new BigDecimal(
                                "1.0000"
                        )
                ).toCommand()
        );
    }
}
