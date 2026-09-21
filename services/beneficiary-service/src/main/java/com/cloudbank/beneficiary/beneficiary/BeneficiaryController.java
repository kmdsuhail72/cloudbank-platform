package com.cloudbank.beneficiary.beneficiary;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BeneficiaryController {

    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        List<BeneficiaryResponse> beneficiaries = service.findAllForCurrentCustomer(authUserId(jwt))
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();

        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/api/v1/beneficiaries/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID beneficiaryId) {
        return service.findByIdForCurrentCustomer(authUserId(jwt), beneficiaryId)
                .map(BeneficiaryResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/beneficiaries")
    public ResponseEntity<BeneficiaryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody BeneficiaryCreateRequest request) {
        Beneficiary beneficiary = service.create(authUserId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BeneficiaryResponse.from(beneficiary));
    }

    @PutMapping("/api/v1/beneficiaries/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID beneficiaryId,
                                                    @Valid @RequestBody BeneficiaryUpdateRequest request) {
        return service.update(authUserId(jwt), beneficiaryId, request)
                .map(BeneficiaryResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/v1/beneficiaries/{beneficiaryId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID beneficiaryId) {
        boolean deleted = service.delete(authUserId(jwt), beneficiaryId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private UUID authUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
