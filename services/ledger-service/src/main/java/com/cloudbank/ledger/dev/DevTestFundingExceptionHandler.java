package com.cloudbank.ledger.dev;

import com.cloudbank.ledger.journal.DuplicateLedgerJournalException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        assignableTypes = DevTestFundingController.class
)
@ConditionalOnProperty(
        prefix = "cloudbank.dev-test-funding",
        name = "enabled",
        havingValue = "true"
)
public class DevTestFundingExceptionHandler {

    @ExceptionHandler(
            DevTestFundingException.class
    )
    public ResponseEntity<Map<String, String>> handleInvalidFunding(
            DevTestFundingException exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_DEV_TEST_FUNDING",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            DuplicateLedgerJournalException.class
    )
    public ResponseEntity<Map<String, String>> handleDuplicateFunding(
            DuplicateLedgerJournalException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "DEV_TEST_FUNDING_REQUEST_EXISTS",
                "Funding request ID already exists"
        );
    }

    private static ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String code,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(
                        Map.of(
                                "error", code,
                                "message", message
                        )
                );
    }
}
