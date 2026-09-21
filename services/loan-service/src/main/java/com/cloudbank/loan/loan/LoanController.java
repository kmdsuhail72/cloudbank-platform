package com.cloudbank.loan.loan;

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
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/loans")
    public ResponseEntity<List<LoanResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        List<LoanResponse> loans = service.findAllForCurrentCustomer(authUserId(jwt))
                .stream()
                .map(LoanResponse::from)
                .toList();

        return ResponseEntity.ok(loans);
    }

    @GetMapping("/api/v1/loans/{loanId}")
    public ResponseEntity<LoanResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID loanId) {
        return service.findByIdForCurrentCustomer(authUserId(jwt), loanId)
                .map(LoanResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/loans")
    public ResponseEntity<LoanResponse> create(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody LoanCreateRequest request) {
        Loan loan = service.create(authUserId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanResponse.from(loan));
    }

    @PatchMapping("/api/v1/loans/{loanId}/status")
    public ResponseEntity<LoanResponse> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID loanId,
                                                   @Valid @RequestBody LoanStatusUpdateRequest request) {
        return service.updateStatus(authUserId(jwt), loanId, request.status())
                .map(LoanResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UUID authUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
