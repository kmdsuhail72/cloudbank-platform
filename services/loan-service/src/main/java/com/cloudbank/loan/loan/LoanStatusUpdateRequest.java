package com.cloudbank.loan.loan;

import jakarta.validation.constraints.NotNull;

public record LoanStatusUpdateRequest(
        @NotNull LoanStatus status
) {
}
