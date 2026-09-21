package com.cloudbank.card.card;

import jakarta.validation.constraints.NotNull;

public record CardStatusUpdateRequest(
        @NotNull
        CardStatus status
) {
}
