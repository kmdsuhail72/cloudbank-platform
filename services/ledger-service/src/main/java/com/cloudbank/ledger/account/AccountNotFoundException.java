package com.cloudbank.ledger.account;

public class AccountNotFoundException
        extends RuntimeException {

    public AccountNotFoundException() {
        super(
                "Account not found"
        );
    }
}
