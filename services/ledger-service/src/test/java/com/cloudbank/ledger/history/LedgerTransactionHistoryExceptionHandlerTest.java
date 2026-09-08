package com.cloudbank.ledger.history;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerTransactionHistoryExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestForInvalidPagination() {
        LedgerTransactionHistoryExceptionHandler handler =
                new LedgerTransactionHistoryExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidRequest(
                        new InvalidTransactionHistoryRequestException(
                                "Size must be between 1 and 100"
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "INVALID_TRANSACTION_HISTORY_REQUEST",
                response.getBody().get(
                        "error"
                )
        );
    }
}
