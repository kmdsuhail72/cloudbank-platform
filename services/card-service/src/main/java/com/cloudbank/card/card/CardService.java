package com.cloudbank.card.card;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository repository;

    public CardService(CardRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Card create(UUID authUserId, CardCreateRequest request) {
        Card card = new Card(
                authUserId,
                request.cardholderName(),
                request.type(),
                request.currency().toUpperCase()
        );
        return repository.saveAndFlush(card);
    }

    @Transactional(readOnly = true)
    public List<Card> findAllForCurrentCustomer(UUID authUserId) {
        return repository.findAllByAuthUserId(authUserId);
    }

    @Transactional(readOnly = true)
    public Optional<Card> findByIdForCurrentCustomer(UUID authUserId, UUID cardId) {
        return repository.findByIdAndAuthUserId(cardId, authUserId);
    }

    @Transactional
    public Optional<Card> updateStatus(UUID authUserId, UUID cardId, CardStatus status) {
        return repository.findByIdAndAuthUserId(cardId, authUserId)
                .map(card -> {
                    card.updateStatus(status);
                    return repository.saveAndFlush(card);
                });
    }
}
