package com.cloudbank.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerGatewayRoutesTest {

    private final CustomerGatewayRoutes customerGatewayRoutes =
            new CustomerGatewayRoutes();

    @Test
    void shouldRouteCustomerProfilePath() {
        RouterFunction<ServerResponse> routes =
                customerGatewayRoutes.customerRoutes(
                        "http://localhost:8083"
                );

        assertTrue(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/customers/me"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldRouteCustomerProfileCreationPath() {
        RouterFunction<ServerResponse> routes =
                customerGatewayRoutes.customerRoutes(
                        "http://localhost:8083"
                );

        assertTrue(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/customers/me"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteCustomerProfileWithUnsupportedHttpMethod() {
        RouterFunction<ServerResponse> routes =
                customerGatewayRoutes.customerRoutes(
                        "http://localhost:8083"
                );

        assertFalse(
                routes.route(
                        request(
                                "PUT",
                                "/api/v1/customers/me"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteUnknownCustomerPath() {
        RouterFunction<ServerResponse> routes =
                customerGatewayRoutes.customerRoutes(
                        "http://localhost:8083"
                );

        assertFalse(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/customers/unknown"
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

        servletRequest.setMethod(method);
        servletRequest.setRequestURI(path);

        return ServerRequest.create(
                servletRequest,
                List.of()
        );
    }
}
