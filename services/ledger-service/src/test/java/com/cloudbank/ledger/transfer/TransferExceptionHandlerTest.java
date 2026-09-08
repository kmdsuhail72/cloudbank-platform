package com.cloudbank.ledger.transfer;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferExceptionHandlerTest {

    private final TransferExceptionHandler handler =
            new TransferExceptionHandler();

    @Test
    void shouldReturnBadRequestForInvalidTransfer() {
        assertError(
                handler.handleInvalidTransfer(
                        new InvalidTransferException(
                                "Invalid transfer"
                        )
                ),
                HttpStatus.BAD_REQUEST,
                "INVALID_TRANSFER"
        );
    }

    @Test
    void shouldReturnConflictForInactiveAccount() {
        assertError(
                handler.handleAccountNotActive(
                        new AccountNotActiveForTransferException(
                                UUID.randomUUID(),
                                "FROZEN"
                        )
                ),
                HttpStatus.CONFLICT,
                "ACCOUNT_NOT_ACTIVE"
        );
    }

    @Test
    void shouldReturnConflictForCurrencyMismatch() {
        assertError(
                handler.handleCurrencyMismatch(
                        new TransferCurrencyMismatchException()
                ),
                HttpStatus.CONFLICT,
                "TRANSFER_CURRENCY_MISMATCH"
        );
    }

    @Test
    void shouldReturnConflictForInsufficientFunds() {
        assertError(
                handler.handleInsufficientFunds(
                        new InsufficientFundsException()
                ),
                HttpStatus.CONFLICT,
                "INSUFFICIENT_FUNDS"
        );
    }

    @Test
    void shouldReturnConflictForIdempotencyMismatch() {
        assertError(
                handler.handleIdempotencyConflict(
                        new TransferIdempotencyConflictException()
                ),
                HttpStatus.CONFLICT,
                "TRANSFER_IDEMPOTENCY_CONFLICT"
        );
    }

    private void assertError(
            ResponseEntity<Map<String, String>> response,
            HttpStatus expectedStatus,
            String expectedCode
    ) {
        assertEquals(
                expectedStatus,
                response.getStatusCode()
        );

        assertEquals(
                expectedCode,
                response.getBody().get(
                        "error"
                )
        );
    }
}
