package com.cloudbank.account.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "customer_id",
            nullable = false,
            updatable = false
    )
    private UUID customerId;

    @Column(
            name = "account_number",
            nullable = false,
            updatable = false,
            length = 34,
            unique = true
    )
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "account_type",
            nullable = false,
            updatable = false,
            length = 20
    )
    private AccountType accountType;

    @Column(
            name = "currency",
            nullable = false,
            updatable = false,
            length = 3
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private AccountStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected Account() {
    }

    public Account(
            UUID customerId,
            String accountNumber,
            AccountType accountType,
            String currency
    ) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = normalizeCurrency(
                currency
        );
        this.status = AccountStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        Instant now =
                databaseTimestamp();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = databaseTimestamp();
    }

    private Instant databaseTimestamp() {
        return Instant
                .now()
                .truncatedTo(
                        ChronoUnit.MICROS
                );
    }

    private String normalizeCurrency(
            String currency
    ) {
        if (currency == null) {
            return null;
        }

        return currency.toUpperCase(
                Locale.ROOT
        );
    }

    public void changeCustomerManagedStatus(
            AccountStatus targetStatus
    ) {
        if (targetStatus == null) {
            throw new IllegalArgumentException(
                    "Target account status is required"
            );
        }

        if (status == AccountStatus.CLOSED
                || targetStatus == AccountStatus.CLOSED) {
            throw new AccountStatusChangeNotAllowedException();
        }

        if (targetStatus != AccountStatus.ACTIVE
                && targetStatus != AccountStatus.FROZEN) {
            throw new AccountStatusChangeNotAllowedException();
        }

        status = targetStatus;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
