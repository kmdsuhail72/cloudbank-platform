package com.cloudbank.account.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerProfileIdentityResponse(

        UUID id

) {
}
