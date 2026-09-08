package com.cloudbank.ledger.history;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class LedgerTransactionHistoryExceptionHandler {

    @ExceptionHandler(
            InvalidTransactionHistoryRequestException.class
    )
    public ResponseEntity<Map<String, String>> handleInvalidRequest(
            InvalidTransactionHistoryRequestException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        Map.of(
                                "error",
                                "INVALID_TRANSACTION_HISTORY_REQUEST",
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(
            TransactionNotFoundException.class
    )
    public ResponseEntity<Map<String, String>> handleTransactionNotFound(
            TransactionNotFoundException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        Map.of(
                                "error",
                                "TRANSACTION_NOT_FOUND",
                                "message",
                                "Transaction not found"
                        )
                );
    }

}