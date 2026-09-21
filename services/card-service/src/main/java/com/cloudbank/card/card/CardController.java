package com.cloudbank.card.card;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/cards")
    public ResponseEntity<List<CardResponse>> list(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<CardResponse> cards = service.findAllForCurrentCustomer(authUserId(jwt))
                .stream()
                .map(CardResponse::from)
                .toList();

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/api/v1/cards/{cardId}")
    public ResponseEntity<CardResponse> get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId
    ) {
        return service.findByIdForCurrentCustomer(authUserId(jwt), cardId)
                .map(CardResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/cards")
    public ResponseEntity<CardResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CardCreateRequest request
    ) {
        Card card = service.create(authUserId(jwt), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CardResponse.from(card));
    }

    private UUID authUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
