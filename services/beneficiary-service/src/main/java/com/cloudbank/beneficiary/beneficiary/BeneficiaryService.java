package com.cloudbank.beneficiary.beneficiary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository repository;

    public BeneficiaryService(BeneficiaryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Beneficiary create(UUID authUserId, BeneficiaryCreateRequest request) {
        Beneficiary beneficiary = new Beneficiary(
                authUserId,
                request.nickname(),
                request.accountHolderName(),
                request.accountNumber(),
                request.bankName(),
                request.currency().toUpperCase()
        );
        return repository.saveAndFlush(beneficiary);
    }

    @Transactional(readOnly = true)
    public List<Beneficiary> findAllForCurrentCustomer(UUID authUserId) {
        return repository.findAllByAuthUserIdOrderByCreatedAtDesc(authUserId);
    }

    @Transactional(readOnly = true)
    public Optional<Beneficiary> findByIdForCurrentCustomer(UUID authUserId, UUID beneficiaryId) {
        return repository.findByIdAndAuthUserId(beneficiaryId, authUserId);
    }

    @Transactional
    public Optional<Beneficiary> update(UUID authUserId, UUID beneficiaryId, BeneficiaryUpdateRequest request) {
        return repository.findByIdAndAuthUserId(beneficiaryId, authUserId)
                .map(beneficiary -> {
                    beneficiary.updateDetails(
                            request.nickname(),
                            request.accountHolderName(),
                            request.accountNumber(),
                            request.bankName(),
                            request.currency().toUpperCase()
                    );
                    return repository.saveAndFlush(beneficiary);
                });
    }

    @Transactional
    public boolean delete(UUID authUserId, UUID beneficiaryId) {
        return repository.findByIdAndAuthUserId(beneficiaryId, authUserId)
                .map(beneficiary -> {
                    repository.delete(beneficiary);
                    return true;
                })
                .orElse(false);
    }
}
