package com.cloudbank.account.customer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CustomerServiceClientConfig {

    @Bean
    public RestClient customerServiceRestClient(
            @Value("${cloudbank.services.customer.base-url}")
            String customerServiceBaseUrl
    ) {
        return RestClient
                .builder()
                .baseUrl(
                        customerServiceBaseUrl
                )
                .build();
    }
}
