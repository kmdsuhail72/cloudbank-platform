package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountCreationService {

    private final AccountRepository repository;

    private final CustomerProfileClient customerProfileClient;

    private final AccountNumberGenerator accountNumberGenerator;

    public AccountCreationService(
            AccountRepository repository,
            CustomerProfileClient customerProfileClient,
            AccountNumberGenerator accountNumberGenerator
    ) {
        this.repository = repository;
        this.customerProfileClient =
                customerProfileClient;
        this.accountNumberGenerator =
                accountNumberGenerator;
    }

    @Transactional
    public Account create(
            String accessToken,
            AccountType accountType,
            String currency
    ) {
        UUID customerId =
                customerProfileClient
                        .findCurrentCustomerId(
                                accessToken
                        )
                        .orElseThrow(
                                CustomerProfileRequiredException::new
                        );

        String accountNumber =
                accountNumberGenerator.generate();

        Account account =
                new Account(
                        customerId,
                        accountNumber,
                        accountType,
                        currency
                );

        return repository.saveAndFlush(
                account
        );
    }
}
