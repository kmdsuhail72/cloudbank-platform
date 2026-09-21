package com.cloudbank.loan.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanCreateRequest(
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 15, fraction = 4) BigDecimal principalAmount,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 4, fraction = 6) BigDecimal annualInterestRate,
        @Positive int termMonths,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull LoanStatus status
) {
}
