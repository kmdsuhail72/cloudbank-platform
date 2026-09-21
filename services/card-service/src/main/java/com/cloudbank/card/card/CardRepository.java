package com.cloudbank.card.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByAuthUserId(UUID authUserId);

    Optional<Card> findByIdAndAuthUserId(UUID id, UUID authUserId);
}
