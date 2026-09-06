package com.cloudbank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class AccountGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> accountRoutes(
            @Value("${cloudbank.services.account.base-url}")
            String accountServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("account-service")
                .GET(
                        "/api/v1/accounts",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/accounts",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                accountServiceBaseUrl
                        )
                )
                .build();
    }
}
