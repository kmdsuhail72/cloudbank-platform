package com.cloudbank.ledger.journal;

public class InvalidJournalException
        extends RuntimeException {

    public InvalidJournalException(
            String message
    ) {
        super(message);
    }
}
