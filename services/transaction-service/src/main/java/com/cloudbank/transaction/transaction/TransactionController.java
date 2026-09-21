package com.cloudbank.transaction.transaction;

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
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/transactions")
    public ResponseEntity<List<TransactionResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        List<TransactionResponse> transactions = service.findAllForCurrentCustomer(authUserId(jwt))
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/api/v1/transactions/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> listForAccount(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID accountId) {
        List<TransactionResponse> transactions = service.findAllForAccount(authUserId(jwt), accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/api/v1/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID transactionId) {
        return service.findByIdForCurrentCustomer(authUserId(jwt), transactionId)
                .map(TransactionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/transactions")
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody TransactionCreateRequest request) {
        Transaction transaction = service.create(authUserId(jwt), jwt.getTokenValue(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction));
    }

    @PatchMapping("/api/v1/transactions/{transactionId}/status")
    public ResponseEntity<TransactionResponse> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable UUID transactionId,
                                                          @Valid @RequestBody TransactionStatusUpdateRequest request) {
        return service.updateStatus(authUserId(jwt), transactionId, request.status())
                .map(TransactionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UUID authUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
