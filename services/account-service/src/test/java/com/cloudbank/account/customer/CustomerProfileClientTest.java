package com.cloudbank.account.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CustomerProfileClientTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private MockRestServiceServer server;

    private CustomerProfileClient client;

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

        RestClient restClient =
                builder
                        .baseUrl(
                                "http://customer.test"
                        )
                        .build();

        client =
                new CustomerProfileClient(
                        restClient
                );
    }

    @Test
    void shouldReturnCustomerIdAndForwardBearerToken() {

        server.expect(
                        once(),
                        requestTo(
                                "http://customer.test/api/v1/customers/me"
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
                                "Bearer valid-token"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "id": "22222222-2222-2222-2222-222222222222",
                                  "authUserId": "11111111-1111-1111-1111-111111111111",
                                  "firstName": "Suhail",
                                  "status": "ACTIVE"
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        Optional<UUID> customerId =
                client.findCurrentCustomerId(
                        "valid-token"
                );

        assertEquals(
                Optional.of(
                        CUSTOMER_ID
                ),
                customerId
        );

        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenCustomerProfileDoesNotExist() {

        server.expect(
                        once(),
                        requestTo(
                                "http://customer.test/api/v1/customers/me"
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
                                "Bearer valid-token"
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus.NOT_FOUND
                        )
                );

        Optional<UUID> customerId =
                client.findCurrentCustomerId(
                        "valid-token"
                );

        assertTrue(
                customerId.isEmpty()
        );

        server.verify();
    }

    @Test
    void shouldPropagateUnexpectedCustomerServiceFailure() {

        server.expect(
                        once(),
                        requestTo(
                                "http://customer.test/api/v1/customers/me"
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.GET
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );

        assertThrows(
                HttpServerErrorException.class,
                () ->
                        client.findCurrentCustomerId(
                                "valid-token"
                        )
        );

        server.verify();
    }
}
