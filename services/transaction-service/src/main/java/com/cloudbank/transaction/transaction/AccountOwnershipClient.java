package com.cloudbank.transaction.transaction;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountOwnershipClient {
    private final RestClient client;

    public AccountOwnershipClient(RestClient.Builder builder,
            @Value("${cloudbank.services.account.base-url}") String baseUrl) {
        client = builder.baseUrl(baseUrl).build();
    }

    public void requireOwnedAccount(String token, UUID accountId, String currency) {
        try {
            Account account = client.get().uri("/api/v1/accounts/{id}", accountId)
                    .headers(headers -> headers.setBearerAuth(token)).retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
                    }).body(Account.class);
            if (account == null || !accountId.equals(account.id())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Account verification failed");
            }
            if (!currency.equals(account.currency())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must match account");
            }
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Account service unavailable");
        }
    }

    public record Account(UUID id, String currency) {}
}
