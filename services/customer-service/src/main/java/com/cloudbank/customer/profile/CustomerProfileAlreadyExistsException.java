package com.cloudbank.customer.profile;

public class CustomerProfileAlreadyExistsException
        extends RuntimeException {

    public CustomerProfileAlreadyExistsException() {
        super("Customer profile already exists");
    }
}
