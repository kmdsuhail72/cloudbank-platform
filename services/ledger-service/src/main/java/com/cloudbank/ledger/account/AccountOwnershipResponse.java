package com.cloudbank.ledger.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
public record AccountOwnershipResponse(
        UUID id,
        String currency,
        String status
) {
}
