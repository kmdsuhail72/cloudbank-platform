package com.cloudbank.account.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountStatusUpdateRequest(

        @NotNull(
                message = "Account status is required"
        )
        @Pattern(
                regexp = "ACTIVE|FROZEN|CLOSED",
                message = "Account status must be ACTIVE, FROZEN, or CLOSED"
        )
        String status

) {
}
