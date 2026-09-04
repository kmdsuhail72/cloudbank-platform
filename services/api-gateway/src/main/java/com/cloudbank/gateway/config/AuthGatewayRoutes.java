package com.cloudbank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class AuthGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> authRoutes(
            @Value("${cloudbank.services.auth.base-url}")
            String authServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("auth-service")
                .route(
                        RequestPredicates.path(
                                "/api/v1/auth/**"
                        ),
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                authServiceBaseUrl
                        )
                )
                .build();
    }
}
