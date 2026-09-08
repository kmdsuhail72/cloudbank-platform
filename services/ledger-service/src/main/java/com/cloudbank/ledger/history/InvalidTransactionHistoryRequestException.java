package com.cloudbank.ledger.history;

public class InvalidTransactionHistoryRequestException
        extends RuntimeException {

    public InvalidTransactionHistoryRequestException(
            String message
    ) {
        super(message);
    }
}
