package com.cloudbank.ledger.history;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerTransactionDetailExceptionHandlerTest {

    @Test
    void shouldReturnPrivacySafeTransactionNotFound() {
        LedgerTransactionHistoryExceptionHandler handler =
                new LedgerTransactionHistoryExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.handleTransactionNotFound(
                        new TransactionNotFoundException()
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "TRANSACTION_NOT_FOUND",
                response.getBody().get(
                        "error"
                )
        );

        assertEquals(
                "Transaction not found",
                response.getBody().get(
                        "message"
                )
        );
    }
}
