package com.cloudbank.beneficiary.beneficiary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "auth_user_id", nullable = false, updatable = false)
    private UUID authUserId;

    @Column(name = "nickname", nullable = false, length = 80)
    private String nickname;

    @Column(name = "account_holder_name", nullable = false, length = 120)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, length = 40)
    private String accountNumber;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Beneficiary() {
    }

    public Beneficiary(UUID authUserId, String nickname, String accountHolderName, String accountNumber,
                      String bankName, String currency) {
        this.id = UUID.randomUUID();
        this.authUserId = authUserId;
        this.nickname = nickname;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.currency = currency;
    }

    @PrePersist
    void onCreate() {
        Instant now = databaseTimestamp();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = databaseTimestamp();
    }

    private Instant databaseTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void updateDetails(String nickname, String accountHolderName, String accountNumber, String bankName, String currency) {
        this.nickname = nickname;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.currency = currency;
    }

    public UUID getId() { return id; }
    public UUID getAuthUserId() { return authUserId; }
    public String getNickname() { return nickname; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getAccountNumber() { return accountNumber; }
    public String getBankName() { return bankName; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
