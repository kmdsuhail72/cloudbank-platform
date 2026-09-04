package com.cloudbank.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthGatewayRoutesTest {

    private final AuthGatewayRoutes authGatewayRoutes =
            new AuthGatewayRoutes();

    @Test
    void shouldRouteAuthApiPaths() {
        RouterFunction<ServerResponse> routes =
                authGatewayRoutes.authRoutes(
                        "http://localhost:8081"
                );

        assertTrue(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/auth/login"
                        )
                ).isPresent()
        );

        assertTrue(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/auth/register"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteUnrelatedApiPaths() {
        RouterFunction<ServerResponse> routes =
                authGatewayRoutes.authRoutes(
                        "http://localhost:8081"
                );

        assertFalse(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/accounts"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteAuthEndpointsWithWrongHttpMethod() {
        RouterFunction<ServerResponse> routes =
                authGatewayRoutes.authRoutes(
                        "http://localhost:8081"
                );

        assertFalse(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/auth/login"
                        )
                ).isPresent()
        );

        assertFalse(
                routes.route(
                        request(
                                "GET",
                                "/api/v1/auth/register"
                        )
                ).isPresent()
        );
    }

    @Test
    void shouldNotRouteUnknownAuthPath() {
        RouterFunction<ServerResponse> routes =
                authGatewayRoutes.authRoutes(
                        "http://localhost:8081"
                );

        assertFalse(
                routes.route(
                        request(
                                "POST",
                                "/api/v1/auth/unknown"
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
