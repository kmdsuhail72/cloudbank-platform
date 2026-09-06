package com.cloudbank.account.account;

public class CustomerProfileRequiredException
        extends RuntimeException {

    public CustomerProfileRequiredException() {
        super(
                "Customer profile is required before creating an account"
        );
    }
}
