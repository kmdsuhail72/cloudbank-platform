package com.cloudbank.ledger.history;

public class TransactionNotFoundException
        extends RuntimeException {

    public TransactionNotFoundException() {
        super(
                "Transaction not found"
        );
    }
}
