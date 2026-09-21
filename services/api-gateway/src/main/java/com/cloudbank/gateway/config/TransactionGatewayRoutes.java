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
public class TransactionGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> transactionRoutes(
            @Value("${cloudbank.services.transaction.base-url}")
            String transactionServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("transaction-service")
                .GET(
                        "/api/v1/transactions",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/transactions/account/{accountId}",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/transactions/{transactionId}",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/transactions",
                        HandlerFunctions.http()
                )
                .PATCH(
                        "/api/v1/transactions/{transactionId}/status",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                transactionServiceBaseUrl
                        )
                )
                .build();
    }
}
