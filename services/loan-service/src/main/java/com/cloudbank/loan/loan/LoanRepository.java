package com.cloudbank.loan.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findAllByAuthUserIdOrderByCreatedAtDesc(UUID authUserId);
    Optional<Loan> findByIdAndAuthUserId(UUID id, UUID authUserId);
}
