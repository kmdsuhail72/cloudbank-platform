package com.cloudbank.ledger.transfer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class TransferExceptionHandler {

    @ExceptionHandler(
            InvalidTransferException.class
    )
    public ResponseEntity<Map<String, String>> handleInvalidTransfer(
            InvalidTransferException exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_TRANSFER",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            AccountNotActiveForTransferException.class
    )
    public ResponseEntity<Map<String, String>> handleAccountNotActive(
            AccountNotActiveForTransferException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "ACCOUNT_NOT_ACTIVE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            TransferCurrencyMismatchException.class
    )
    public ResponseEntity<Map<String, String>> handleCurrencyMismatch(
            TransferCurrencyMismatchException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "TRANSFER_CURRENCY_MISMATCH",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            InsufficientFundsException.class
    )
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(
            InsufficientFundsException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "INSUFFICIENT_FUNDS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            TransferIdempotencyConflictException.class
    )
    public ResponseEntity<Map<String, String>> handleIdempotencyConflict(
            TransferIdempotencyConflictException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "TRANSFER_IDEMPOTENCY_CONFLICT",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_JSON",
                "Request body is malformed or contains invalid values"
        );
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String code,
            String message
    ) {
        return ResponseEntity
                .status(
                        status
                )
                .body(
                        Map.of(
                                "error",
                                code,
                                "message",
                                message
                        )
                );
    }
}
