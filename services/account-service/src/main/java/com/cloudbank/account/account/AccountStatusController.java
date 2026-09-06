package com.cloudbank.account.account;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AccountStatusController {

    private final AccountStatusService statusService;

    public AccountStatusController(
            AccountStatusService statusService
    ) {
        this.statusService =
                statusService;
    }

    @PatchMapping(
            "/api/v1/accounts/{accountId}/status"
    )
    public ResponseEntity<AccountResponse> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @Valid @RequestBody
            AccountStatusUpdateRequest request
    ) {
        Account account =
                statusService
                        .updateCurrentCustomerAccountStatus(
                                jwt.getTokenValue(),
                                accountId,
                                request.status()
                        );

        return ResponseEntity.ok(
                AccountResponse.from(
                        account
                )
        );
    }
}
