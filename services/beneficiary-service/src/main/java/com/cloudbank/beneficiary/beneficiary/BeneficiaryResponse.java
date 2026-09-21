package com.cloudbank.beneficiary.beneficiary;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponse(
        UUID id,
        UUID authUserId,
        String nickname,
        String accountHolderName,
        String accountNumber,
        String bankName,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
    public static BeneficiaryResponse from(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getAuthUserId(),
                beneficiary.getNickname(),
                beneficiary.getAccountHolderName(),
                beneficiary.getAccountNumber(),
                beneficiary.getBankName(),
                beneficiary.getCurrency(),
                beneficiary.getCreatedAt(),
                beneficiary.getUpdatedAt()
        );
    }
}
