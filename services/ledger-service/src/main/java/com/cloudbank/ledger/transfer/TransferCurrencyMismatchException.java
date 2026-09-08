package com.cloudbank.ledger.transfer;

public class TransferCurrencyMismatchException
        extends RuntimeException {

    public TransferCurrencyMismatchException() {
        super(
                "Source and destination accounts must use the same currency"
        );
    }
}
