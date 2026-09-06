package com.cloudbank.account.account;

import com.cloudbank.account.customer.CustomerProfileClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountStatusService {

    private final AccountRepository repository;

    private final CustomerProfileClient customerProfileClient;

    public AccountStatusService(
            AccountRepository repository,
            CustomerProfileClient customerProfileClient
    ) {
        this.repository = repository;
        this.customerProfileClient =
                customerProfileClient;
    }

    @Transactional
    public Account updateCurrentCustomerAccountStatus(
            String accessToken,
            UUID accountId,
            AccountStatus targetStatus
    ) {
        UUID customerId =
                customerProfileClient
                        .findCurrentCustomerId(
                                accessToken
                        )
                        .orElseThrow(
                                CustomerProfileRequiredException::new
                        );

        Account account =
                repository
                        .findByIdAndCustomerId(
                                accountId,
                                customerId
                        )
                        .orElseThrow(
                                AccountNotFoundException::new
                        );

        account.changeCustomerManagedStatus(
                targetStatus
        );

        return repository.saveAndFlush(
                account
        );
    }
}
