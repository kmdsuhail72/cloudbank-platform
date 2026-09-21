package com.cloudbank.beneficiary.beneficiary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    List<Beneficiary> findAllByAuthUserIdOrderByCreatedAtDesc(UUID authUserId);
    Optional<Beneficiary> findByIdAndAuthUserId(UUID id, UUID authUserId);
}
