package com.cloudbank.beneficiary.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BeneficiaryUpdateRequest(
        @NotBlank @Size(max = 80) String nickname,
        @NotBlank @Size(max = 120) String accountHolderName,
        @NotBlank @Size(max = 40) String accountNumber,
        @Size(max = 120) String bankName,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}
