package com.cloudbank.transaction.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "auth_user_id", nullable = false, updatable = false)
    private UUID authUserId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "counterparty_account_id")
    private UUID counterpartyAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Transaction() {
    }

    public Transaction(UUID authUserId, UUID accountId, UUID counterpartyAccountId, BigDecimal amount, String currency,
                      String description, TransactionType type, TransactionStatus status) {
        this.id = UUID.randomUUID();
        this.authUserId = authUserId;
        this.accountId = accountId;
        this.counterpartyAccountId = counterpartyAccountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.type = type;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = databaseTimestamp();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = TransactionStatus.POSTED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = databaseTimestamp();
    }

    private Instant databaseTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getAuthUserId() { return authUserId; }
    public UUID getAccountId() { return accountId; }
    public UUID getCounterpartyAccountId() { return counterpartyAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
