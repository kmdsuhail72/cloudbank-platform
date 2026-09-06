package com.cloudbank.ledger.journal;

public class DuplicateLedgerJournalException
        extends RuntimeException {

    public DuplicateLedgerJournalException() {
        super(
                "Ledger journal reference already exists"
        );
    }
}
