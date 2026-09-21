package com.cloudbank.card.card;

import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID authUserId,
        String cardholderName,
        CardType type,
        CardStatus status,
        String maskedNumber,
        String currency,
        int expirationMonth,
        int expirationYear,
        Instant createdAt,
        Instant updatedAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getAuthUserId(),
                card.getCardholderName(),
                card.getType(),
                card.getStatus(),
                card.getMaskedNumber(),
                card.getCurrency(),
                card.getExpirationMonth(),
                card.getExpirationYear(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
