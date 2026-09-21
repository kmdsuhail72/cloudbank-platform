package com.cloudbank.loan.loan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanRepository repository;

    public LoanService(LoanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Loan create(UUID authUserId, LoanCreateRequest request) {
        if (request.status() != LoanStatus.APPLICATION) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "New loans must be applications");
        }
        Loan loan = new Loan(
                authUserId,
                request.principalAmount(),
                request.annualInterestRate(),
                request.termMonths(),
                request.currency().toUpperCase(),
                request.status()
        );
        return repository.saveAndFlush(loan);
    }

    @Transactional(readOnly = true)
    public List<Loan> findAllForCurrentCustomer(UUID authUserId) {
        return repository.findAllByAuthUserIdOrderByCreatedAtDesc(authUserId);
    }

    @Transactional(readOnly = true)
    public Optional<Loan> findByIdForCurrentCustomer(UUID authUserId, UUID loanId) {
        return repository.findByIdAndAuthUserId(loanId, authUserId);
    }

    @Transactional
    public Optional<Loan> updateStatus(UUID authUserId, UUID loanId, LoanStatus status) {
        return repository.findByIdAndAuthUserId(loanId, authUserId)
                .map(loan -> {
                    if (loan.getStatus() != LoanStatus.APPLICATION || status != LoanStatus.REJECTED) {
                        throw new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.CONFLICT,
                                "Customers may only withdraw pending applications");
                    }
                    loan.updateStatus(status);
                    return repository.saveAndFlush(loan);
                });
    }
}
