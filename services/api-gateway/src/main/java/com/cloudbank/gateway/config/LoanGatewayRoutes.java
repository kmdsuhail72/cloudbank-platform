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
public class LoanGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> loanRoutes(
            @Value("${cloudbank.services.loan.base-url}")
            String loanServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("loan-service")
                .GET(
                        "/api/v1/loans",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/loans/{loanId}",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/loans",
                        HandlerFunctions.http()
                )
                .PATCH(
                        "/api/v1/loans/{loanId}/status",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                loanServiceBaseUrl
                        )
                )
                .build();
    }
}
