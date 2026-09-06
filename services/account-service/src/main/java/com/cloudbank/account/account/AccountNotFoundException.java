package com.cloudbank.account.account;

public class AccountNotFoundException
        extends RuntimeException {

    public AccountNotFoundException() {
        super(
                "Account not found"
        );
    }
}
