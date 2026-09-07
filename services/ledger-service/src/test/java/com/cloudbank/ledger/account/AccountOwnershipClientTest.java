package com.cloudbank.ledger.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AccountOwnershipClientTest {

    private static final String BASE_URL =
            "http://account-service.test";

    private MockRestServiceServer server;

    private AccountOwnershipClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder =
                RestClient.builder();

        server =
                MockRestServiceServer
                        .bindTo(
                                builder
                        )
                        .build();

        client =
                new AccountOwnershipClient(
                        builder,
                        BASE_URL
                );
    }

    @Test
    void shouldForwardBearerTokenAndReturnOwnedAccount() {
        UUID accountId =
                UUID.randomUUID();

        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/api/v1/accounts/"
                                        + accountId
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.GET
                        )
                )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer access-token"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "id": "%s",
                                  "accountNumber": "100000000001",
                                  "accountType": "CHECKING",
                                  "currency": "INR",
                                  "status": "ACTIVE"
                                }
                                """.formatted(
                                        accountId
                                ),
                                MediaType.APPLICATION_JSON
                        )
                );

        AccountOwnershipResponse response =
                client.requireOwnedAccount(
                        "access-token",
                        accountId
                );

        assertEquals(
                accountId,
                response.id()
        );

        assertEquals(
                "INR",
                response.currency()
        );

        server.verify();
    }

    @Test
    void shouldTranslateOwnershipSafeNotFound() {
        UUID accountId =
                UUID.randomUUID();

        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/api/v1/accounts/"
                                        + accountId
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus.NOT_FOUND
                        )
                );

        assertThrows(
                AccountNotFoundException.class,
                () -> client.requireOwnedAccount(
                        "access-token",
                        accountId
                )
        );

        server.verify();
    }

    @Test
    void shouldTranslateAccountServiceFailure() {
        UUID accountId =
                UUID.randomUUID();

        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/api/v1/accounts/"
                                        + accountId
                        )
                )
                .andRespond(
                        withServerError()
                );

        assertThrows(
                AccountOwnershipVerificationException.class,
                () -> client.requireOwnedAccount(
                        "access-token",
                        accountId
                )
        );

        server.verify();
    }
}
