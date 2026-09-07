package com.cloudbank.ledger.account;

public class AccountOwnershipVerificationException
        extends RuntimeException {

    public AccountOwnershipVerificationException() {
        super(
                "Account ownership could not be verified"
        );
    }

    public AccountOwnershipVerificationException(
            Throwable cause
    ) {
        super(
                "Account ownership could not be verified",
                cause
        );
    }
}
