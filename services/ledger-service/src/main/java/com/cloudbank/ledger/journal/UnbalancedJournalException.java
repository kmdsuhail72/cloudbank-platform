package com.cloudbank.ledger.journal;

public class UnbalancedJournalException
        extends RuntimeException {

    public UnbalancedJournalException() {
        super(
                "Ledger journal debits and credits must balance"
        );
    }
}
