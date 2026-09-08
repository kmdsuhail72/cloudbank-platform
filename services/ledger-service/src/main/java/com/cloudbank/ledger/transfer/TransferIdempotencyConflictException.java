package com.cloudbank.ledger.transfer;

public class TransferIdempotencyConflictException
        extends RuntimeException {

    public TransferIdempotencyConflictException() {
        super(
                "Transfer request ID was already used for different transfer details"
        );
    }
}
