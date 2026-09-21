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
public class BeneficiaryGatewayRoutes {

    @Bean
    public RouterFunction<ServerResponse> beneficiaryRoutes(
            @Value("${cloudbank.services.beneficiary.base-url}")
            String beneficiaryServiceBaseUrl
    ) {
        return GatewayRouterFunctions
                .route("beneficiary-service")
                .GET(
                        "/api/v1/beneficiaries",
                        HandlerFunctions.http()
                )
                .GET(
                        "/api/v1/beneficiaries/{beneficiaryId}",
                        HandlerFunctions.http()
                )
                .POST(
                        "/api/v1/beneficiaries",
                        HandlerFunctions.http()
                )
                .PUT(
                        "/api/v1/beneficiaries/{beneficiaryId}",
                        HandlerFunctions.http()
                )
                .DELETE(
                        "/api/v1/beneficiaries/{beneficiaryId}",
                        HandlerFunctions.http()
                )
                .before(
                        BeforeFilterFunctions.uri(
                                beneficiaryServiceBaseUrl
                        )
                )
                .build();
    }
}
