package com.cloudbank.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerGatewayRoutesTest {

    private final LedgerGatewayRoutes ledgerGatewayRoutes =
            new LedgerGatewayRoutes();

    @Test
    void shouldRouteAccountBalancePath() {
        RouterFunction<ServerResponse> routes =
                ledgerGatewayRoutes.ledgerRoutes(
                        "http://localhost:8085"
                );

        assertTrue(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/accounts/"
                                        + "33333333-3333-3333-3333-333333333333"
                                        + "/balance"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteAccountBalanceWithPost() {
        RouterFunction<ServerResponse> routes =
                ledgerGatewayRoutes.ledgerRoutes(
                        "http://localhost:8085"
                );

        assertFalse(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/accounts/"
                                        + "33333333-3333-3333-3333-333333333333"
                                        + "/balance"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotClaimNormalAccountDetailPath() {
        RouterFunction<ServerResponse> routes =
                ledgerGatewayRoutes.ledgerRoutes(
                        "http://localhost:8085"
                );

        assertFalse(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/accounts/"
                                        + "33333333-3333-3333-3333-333333333333"
                        )
                ).isPresent()
        );
    }

    private ServerRequest request(
            String method,
            String path
    ) {
        MockHttpServletRequest servletRequest =
                new MockHttpServletRequest();

        servletRequest.setMethod(
                method
        );

        servletRequest.setRequestURI(
                path
        );

        return ServerRequest.create(
                servletRequest,
                List.of()
        );
    }
}
