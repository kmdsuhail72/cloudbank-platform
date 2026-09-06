package com.cloudbank.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountGatewayRoutesTest {

    private final AccountGatewayRoutes accountGatewayRoutes =
            new AccountGatewayRoutes();

    @Test
    void shouldRouteAccountCreationPath() {
        RouterFunction<ServerResponse> routes =
                accountGatewayRoutes.accountRoutes(
                        "http://localhost:8084"
                );

        assertTrue(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/accounts"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldRouteAccountListPath() {
        RouterFunction<ServerResponse> routes =
                accountGatewayRoutes.accountRoutes(
                        "http://localhost:8084"
                );

        assertTrue(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/accounts"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteAccountCreationWithPatch() {
        RouterFunction<ServerResponse> routes =
                accountGatewayRoutes.accountRoutes(
                        "http://localhost:8084"
                );

        assertFalse(
                routes.route(
                        request(
                                "PATCH",
                                "/api/v1/accounts"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteUnknownAccountPath() {
        RouterFunction<ServerResponse> routes =
                accountGatewayRoutes.accountRoutes(
                        "http://localhost:8084"
                );

        assertFalse(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/accounts/unknown"
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
