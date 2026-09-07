package com.cloudbank.ledger.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.UUID;

@Component
public class AccountOwnershipClient {

    private final RestClient restClient;

    public AccountOwnershipClient(
            RestClient.Builder restClientBuilder,
            @Value(
                    "${cloudbank.services.account.base-url}"
            )
            String accountServiceBaseUrl
    ) {
        Objects.requireNonNull(
                restClientBuilder,
                "RestClient builder is required"
        );

        this.restClient =
                restClientBuilder
                        .baseUrl(
                                requireBaseUrl(
                                        accountServiceBaseUrl
                                )
                        )
                        .build();
    }

    public AccountOwnershipResponse requireOwnedAccount(
            String accessToken,
            UUID accountId
    ) {
        if (accessToken == null
                || accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token is required"
            );
        }

        Objects.requireNonNull(
                accountId,
                "Account ID is required"
        );

        try {
            AccountOwnershipResponse response =
                    restClient
                            .get()
                            .uri(
                                    "/api/v1/accounts/{accountId}",
                                    accountId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + accessToken
                            )
                            .retrieve()
                            .onStatus(
                                    status ->
                                            status.value() == 404,
                                    (request, responseValue) -> {
                                        throw new AccountNotFoundException();
                                    }
                            )
                            .onStatus(
                                    HttpStatusCode::isError,
                                    (request, responseValue) -> {
                                        throw new AccountOwnershipVerificationException();
                                    }
                            )
                            .body(
                                    AccountOwnershipResponse.class
                            );

            validateResponse(
                    accountId,
                    response
            );

            return response;

        } catch (AccountNotFoundException
                 | AccountOwnershipVerificationException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new AccountOwnershipVerificationException(
                    exception
            );
        }
    }

    private static void validateResponse(
            UUID requestedAccountId,
            AccountOwnershipResponse response
    ) {
        if (response == null
                || response.id() == null
                || !requestedAccountId.equals(
                        response.id()
                )
                || response.currency() == null
                || response.currency().isBlank()
                || response.currency().trim().length() != 3) {

            throw new AccountOwnershipVerificationException();
        }
    }

    private static String requireBaseUrl(
            String baseUrl
    ) {
        if (baseUrl == null
                || baseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Account Service base URL is required"
            );
        }

        String normalized =
                baseUrl.trim();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized;
    }
}
