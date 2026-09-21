package com.cloudbank.transaction.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountOwnershipClient accounts;

    public TransactionService(TransactionRepository repository, AccountOwnershipClient accounts) {
        this.repository = repository;
        this.accounts = accounts;
    }

    @Transactional
    public Transaction create(UUID authUserId, String token, TransactionCreateRequest request) {
        accounts.requireOwnedAccount(token, request.accountId(), request.currency());
        Transaction transaction = new Transaction(
                authUserId,
                request.accountId(),
                request.counterpartyAccountId(),
                request.amount(),
                request.currency().toUpperCase(),
                request.description(),
                request.type(),
                request.status()
        );
        return repository.saveAndFlush(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAllForCurrentCustomer(UUID authUserId) {
        return repository.findAllByAuthUserIdOrderByCreatedAtDesc(authUserId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAllForAccount(UUID authUserId, UUID accountId) {
        return repository.findAllByAccountIdAndAuthUserIdOrderByCreatedAtDesc(accountId, authUserId);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findByIdForCurrentCustomer(UUID authUserId, UUID transactionId) {
        return repository.findByIdAndAuthUserId(transactionId, authUserId);
    }

    @Transactional
    public Optional<Transaction> updateStatus(UUID authUserId, UUID transactionId, TransactionStatus status) {
        return repository.findByIdAndAuthUserId(transactionId, authUserId)
                .map(transaction -> {
                    transaction.updateStatus(status);
                    return repository.saveAndFlush(transaction);
                });
    }
}
