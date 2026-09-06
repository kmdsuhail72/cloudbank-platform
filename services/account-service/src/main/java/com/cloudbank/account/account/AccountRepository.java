package com.cloudbank.account.account;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(
            String accountNumber
    );

    List<Account> findByCustomerId(
            UUID customerId
    );

    Optional<Account> findByIdAndCustomerId(
            UUID id,
            UUID customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from Account account
            where account.id = :accountId
              and account.customerId = :customerId
            """)
    Optional<Account> findForUpdateByIdAndCustomerId(
            @Param("accountId")
            UUID accountId,
            @Param("customerId")
            UUID customerId
    );

    boolean existsByAccountNumber(
            String accountNumber
    );
}
