package com.cloudbank.account.account;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    private final AccountCreationService service;

    public AccountController(
            AccountCreationService service
    ) {
        this.service = service;
    }

    @PostMapping("/api/v1/accounts")
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountCreateRequest request
    ) {
        Account account =
                service.create(
                        jwt.getTokenValue(),
                        request.accountType(),
                        request.currency()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        AccountResponse.from(
                                account
                        )
                );
    }
}
