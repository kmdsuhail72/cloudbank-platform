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
public class CustomerGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> customerRoutes(
            @Value("${cloudbank.services.customer.base-url}")
            String customerServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("customer-service")
                .GET(
                        "/api/v1/customers/me",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/customers/me",
                        HandlerFunctions.http()
                )
                .PUT(
                        "/api/v1/customers/me",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                customerServiceBaseUrl
                        )
                )
                .build();
    }
}
