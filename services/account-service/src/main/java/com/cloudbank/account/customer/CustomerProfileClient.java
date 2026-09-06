package com.cloudbank.account.customer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerProfileClient {

    private final RestClient restClient;

    public CustomerProfileClient(
            @Qualifier("customerServiceRestClient")
            RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public Optional<UUID> findCurrentCustomerId(
            String accessToken
    ) {
        try {
            CustomerProfileIdentityResponse response =
                    restClient
                            .get()
                            .uri(
                                    "/api/v1/customers/me"
                            )
                            .headers(headers ->
                                    headers.setBearerAuth(
                                            accessToken
                                    )
                            )
                            .retrieve()
                            .body(
                                    CustomerProfileIdentityResponse.class
                            );

            if (
                    response == null
                            || response.id() == null
            ) {
                throw new IllegalStateException(
                        "Customer Service response did not contain customer id"
                );
            }

            return Optional.of(
                    response.id()
            );
        } catch (
                HttpClientErrorException.NotFound exception
        ) {
            return Optional.empty();
        }
    }
}
