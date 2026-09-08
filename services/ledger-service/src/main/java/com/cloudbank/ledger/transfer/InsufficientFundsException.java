package com.cloudbank.ledger.transfer;

public class InsufficientFundsException
        extends RuntimeException {

    public InsufficientFundsException() {
        super(
                "Source account has insufficient posted funds"
        );
    }
}
