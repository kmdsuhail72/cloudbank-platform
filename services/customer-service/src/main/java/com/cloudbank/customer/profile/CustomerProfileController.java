package com.cloudbank.customer.profile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        return service
                .findByAuthUserId(
                        authUserId(jwt)
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

    @PostMapping("/api/v1/customers/me")
    public ResponseEntity<CustomerProfileResponse> create(
            @AuthenticationPrincipal Jwt jwt
    ) {
        CustomerProfile profile =
                service.create(
                        authUserId(jwt)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CustomerProfileResponse.from(
                                profile
                        )
                );
    }

    private UUID authUserId(
            Jwt jwt
    ) {
        return UUID.fromString(
                jwt.getSubject()
        );
    }
}
