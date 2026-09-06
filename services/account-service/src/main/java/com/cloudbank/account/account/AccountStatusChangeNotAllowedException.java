package com.cloudbank.account.account;

public class AccountStatusChangeNotAllowedException
        extends RuntimeException {

    public AccountStatusChangeNotAllowedException() {
        super(
                "Requested account status change is not allowed"
        );
    }
}
