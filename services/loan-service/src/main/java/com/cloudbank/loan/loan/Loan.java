package com.cloudbank.loan.loan;

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
@Table(name = "loans")
public class Loan {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "auth_user_id", nullable = false, updatable = false)
    private UUID authUserId;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "annual_interest_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal annualInterestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Loan() {
    }

    public Loan(UUID authUserId, BigDecimal principalAmount, BigDecimal annualInterestRate, int termMonths, String currency, LoanStatus status) {
        this.id = UUID.randomUUID();
        this.authUserId = authUserId;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.currency = currency;
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
            status = LoanStatus.APPLICATION;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = databaseTimestamp();
    }

    private Instant databaseTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void updateStatus(LoanStatus status) {
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getAuthUserId() { return authUserId; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public int getTermMonths() { return termMonths; }
    public String getCurrency() { return currency; }
    public LoanStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
