package com.cloudbank.ledger.journal;

import com.cloudbank.ledger.account.AccountNotFoundException;
import com.cloudbank.ledger.account.AccountOwnershipVerificationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class LedgerBalanceExceptionHandler {

    @ExceptionHandler(
            AccountNotFoundException.class
    )
    public ResponseEntity<Map<String, String>> handleAccountNotFound(
            AccountNotFoundException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        Map.of(
                                "error",
                                "ACCOUNT_NOT_FOUND",
                                "message",
                                "Account not found"
                        )
                );
    }

    @ExceptionHandler(
            AccountOwnershipVerificationException.class
    )
    public ResponseEntity<Map<String, String>> handleOwnershipVerificationFailure(
            AccountOwnershipVerificationException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.BAD_GATEWAY
                )
                .body(
                        Map.of(
                                "error",
                                "ACCOUNT_SERVICE_UNAVAILABLE",
                                "message",
                                "Account ownership could not be verified"
                        )
                );
    }
}
