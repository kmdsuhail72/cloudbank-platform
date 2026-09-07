package com.cloudbank.ledger.journal;

import com.cloudbank.ledger.account.AccountNotFoundException;
import com.cloudbank.ledger.account.AccountOwnershipVerificationException;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerBalanceExceptionHandlerTest {

    private final LedgerBalanceExceptionHandler handler =
            new LedgerBalanceExceptionHandler();

    @Test
    void shouldReturnOwnershipSafeNotFoundContract() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccountNotFound(
                        new AccountNotFoundException()
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "ACCOUNT_NOT_FOUND",
                response.getBody().get(
                        "error"
                )
        );

        assertEquals(
                "Account not found",
                response.getBody().get(
                        "message"
                )
        );
    }

    @Test
    void shouldReturnBadGatewayWhenOwnershipCannotBeVerified() {
        ResponseEntity<Map<String, String>> response =
                handler.handleOwnershipVerificationFailure(
                        new AccountOwnershipVerificationException()
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );

        assertEquals(
                "ACCOUNT_SERVICE_UNAVAILABLE",
                response.getBody().get(
                        "error"
                )
        );
    }
}
