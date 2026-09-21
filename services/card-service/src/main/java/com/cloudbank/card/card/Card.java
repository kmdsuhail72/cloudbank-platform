package com.cloudbank.card.card;

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
import java.util.UUID;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "auth_user_id", nullable = false, updatable = false)
    private UUID authUserId;

    @Column(name = "cardholder_name", nullable = false, length = 120)
    private String cardholderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "masked_number", nullable = false, length = 32)
    private String maskedNumber;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "expiration_month", nullable = false)
    private int expirationMonth;

    @Column(name = "expiration_year", nullable = false)
    private int expirationYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Card() {
    }

    public Card(UUID authUserId, String cardholderName, CardType type, String currency) {
        this.id = UUID.randomUUID();
        this.authUserId = authUserId;
        this.cardholderName = cardholderName;
        this.type = type;
        this.status = CardStatus.ACTIVE;
        this.currency = currency;
        this.maskedNumber = generateMaskedNumber();
        java.time.YearMonth expiry = java.time.YearMonth.now(java.time.ZoneOffset.UTC).plusYears(3);
        this.expirationMonth = expiry.getMonthValue();
        this.expirationYear = expiry.getYear();
    }

    @PrePersist
    void onCreate() {
        Instant now = databaseTimestamp();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = CardStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = databaseTimestamp();
    }

    private Instant databaseTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private String generateMaskedNumber() {
        return "•••• " + String.format("%04d", Math.floorMod(UUID.randomUUID().hashCode(), 10000));
    }

    public void updateStatus(CardStatus nextStatus) {
        this.status = nextStatus;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public CardType getType() {
        return type;
    }

    public CardStatus getStatus() {
        return status;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public int getExpirationMonth() {
        return expirationMonth;
    }

    public int getExpirationYear() {
        return expirationYear;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
