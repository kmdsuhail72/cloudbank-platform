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
public class LedgerGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> ledgerRoutes(
            @Value("${cloudbank.services.ledger.base-url}")
            String ledgerServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("ledger-service")
                .GET(
                        "/api/v1/accounts/{accountId}/balance",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/accounts/{accountId}/transactions",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/accounts/{accountId}/transactions/{postingId}",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/transfers",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                ledgerServiceBaseUrl
                        )
                )
                .build();
    }
}
