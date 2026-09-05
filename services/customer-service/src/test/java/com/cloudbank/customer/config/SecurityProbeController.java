package com.cloudbank.customer.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityProbeController {

    @GetMapping("/api/v1/customers/me")
    String customerProfile() {
        return "customer";
    }

    @GetMapping("/actuator/health")
    String health() {
        return "UP";
    }

    @GetMapping("/error")
    String directError() {
        return "error";
    }

    @GetMapping("/internal/probe")
    String internalProbe() {
        return "internal";
    }
}
