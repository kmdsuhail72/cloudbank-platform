package com.cloudbank.card.card;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CardStatusController {

    private final CardService service;

    public CardStatusController(CardService service) {
        this.service = service;
    }

    @PatchMapping("/api/v1/cards/{cardId}/status")
    public ResponseEntity<CardResponse> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId,
            @Valid @RequestBody CardStatusUpdateRequest request
    ) {
        CardStatus status = request.status();

        return service.updateStatus(authUserId(jwt), cardId, status)
                .map(CardResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UUID authUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
