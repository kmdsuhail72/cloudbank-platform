package com.cloudbank.account.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @Test
    void shouldPersistAndFindAccountByCustomerId() {
        UUID customerId =
                UUID.randomUUID();

        Account account =
                new Account(
                        customerId,
                        "CBTEST000000000001",
                        AccountType.SAVINGS,
                        "inr"
                );

        Account saved =
                repository.saveAndFlush(
                        account
                );

        assertNotNull(
                saved.getId()
        );

        assertEquals(
                customerId,
                saved.getCustomerId()
        );

        assertEquals(
                "CBTEST000000000001",
                saved.getAccountNumber()
        );

        assertEquals(
                AccountType.SAVINGS,
                saved.getAccountType()
        );

        assertEquals(
                "INR",
                saved.getCurrency()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                saved.getStatus()
        );

        assertNotNull(
                saved.getCreatedAt()
        );

        assertNotNull(
                saved.getUpdatedAt()
        );

        List<Account> customerAccounts =
                repository.findByCustomerId(
                        customerId
                );

        assertEquals(
                1,
                customerAccounts.size()
        );

        assertTrue(
                repository
                        .findByAccountNumber(
                                "CBTEST000000000001"
                        )
                        .isPresent()
        );

        assertTrue(
                repository
                        .existsByAccountNumber(
                                "CBTEST000000000001"
                        )
        );
    }
}
