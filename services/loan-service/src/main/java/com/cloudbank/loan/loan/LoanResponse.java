package com.cloudbank.loan.loan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanResponse(
        UUID id,
        UUID authUserId,
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        int termMonths,
        String currency,
        LoanStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getAuthUserId(),
                loan.getPrincipalAmount(),
                loan.getAnnualInterestRate(),
                loan.getTermMonths(),
                loan.getCurrency(),
                loan.getStatus(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }
}
