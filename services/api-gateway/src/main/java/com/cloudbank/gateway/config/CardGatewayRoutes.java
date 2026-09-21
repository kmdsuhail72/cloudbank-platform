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
public class CardGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> cardRoutes(
            @Value("${cloudbank.services.card.base-url}")
            String cardServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("card-service")
                .GET(
                        "/api/v1/cards",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/cards/{cardId}",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/cards",
                        HandlerFunctions.http()
                )
                .PATCH(
                        "/api/v1/cards/{cardId}/status",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                cardServiceBaseUrl
                        )
                )
                .build();
    }
}
