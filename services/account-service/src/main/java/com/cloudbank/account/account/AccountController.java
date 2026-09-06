package com.cloudbank.account.account;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AccountController {

    private final AccountCreationService creationService;

    private final AccountQueryService queryService;

    public AccountController(
            AccountCreationService creationService,
            AccountQueryService queryService
    ) {
        this.creationService =
                creationService;
        this.queryService =
                queryService;
    }

    @PostMapping("/api/v1/accounts")
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountCreateRequest request
    ) {
        Account account =
                creationService.create(
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

    @GetMapping("/api/v1/accounts")
    public ResponseEntity<List<AccountResponse>> list(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<AccountResponse> accounts =
                queryService
                        .findCurrentCustomerAccounts(
                                jwt.getTokenValue()
                        )
                        .stream()
                        .map(
                                AccountResponse::from
                        )
                        .toList();

        return ResponseEntity.ok(
                accounts
        );
    }

    @GetMapping("/api/v1/accounts/{accountId}")
    public ResponseEntity<AccountResponse> get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId
    ) {
        Account account =
                queryService
                        .findCurrentCustomerAccount(
                                jwt.getTokenValue(),
                                accountId
                        );

        return ResponseEntity.ok(
                AccountResponse.from(
                        account
                )
        );
    }
}
