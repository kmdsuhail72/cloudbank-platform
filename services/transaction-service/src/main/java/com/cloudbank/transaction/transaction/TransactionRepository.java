package com.cloudbank.transaction.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByAuthUserIdOrderByCreatedAtDesc(UUID authUserId);
    List<Transaction> findAllByAccountIdAndAuthUserIdOrderByCreatedAtDesc(UUID accountId, UUID authUserId);
    Optional<Transaction> findByIdAndAuthUserId(UUID id, UUID authUserId);
}
