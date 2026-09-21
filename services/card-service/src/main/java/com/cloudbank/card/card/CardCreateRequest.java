package com.cloudbank.card.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardCreateRequest(
        @NotBlank
        @Size(max = 120)
        String cardholderName,

        @NotNull
        CardType type,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency
) {
}
