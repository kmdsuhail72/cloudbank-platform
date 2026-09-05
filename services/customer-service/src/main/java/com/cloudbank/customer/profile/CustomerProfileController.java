package com.cloudbank.customer.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CustomerProfileController {

    private final CustomerProfileService service;

    public CustomerProfileController(
            CustomerProfileService service
    ) {
        this.service = service;
    }

    @GetMapping("/api/v1/customers/me")
    public ResponseEntity<CustomerProfileResponse> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return service
                .findByAuthUserId(
                        authUserId
                )
                .map(
                        CustomerProfileResponse::from
                )
                .map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }
}
