package com.cloudbank.account.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountCreateRequest(

        @NotNull(
                message = "Account type is required"
        )
        AccountType accountType,

        @NotBlank(
                message = "Currency is required"
        )
        @Pattern(
                regexp = "[A-Za-z]{3}",
                message = "Currency must be exactly 3 letters"
        )
        String currency

) {
}
