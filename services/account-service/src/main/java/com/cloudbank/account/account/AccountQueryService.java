package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountQueryService {

    private final AccountRepository repository;

    private final CustomerProfileClient customerProfileClient;

    public AccountQueryService(
            AccountRepository repository,
            CustomerProfileClient customerProfileClient
    ) {
        this.repository = repository;
        this.customerProfileClient =
                customerProfileClient;
    }

    @Transactional(readOnly = true)
    public List<Account> findCurrentCustomerAccounts(
            String accessToken
    ) {
        UUID customerId =
                customerProfileClient
                        .findCurrentCustomerId(
                                accessToken
                        )
                        .orElseThrow(
                                CustomerProfileRequiredException::new
                        );

        return repository.findByCustomerId(
                customerId
        );
    }
}
