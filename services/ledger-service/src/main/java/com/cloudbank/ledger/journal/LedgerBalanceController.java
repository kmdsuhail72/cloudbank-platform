package com.cloudbank.ledger.journal;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
public class LedgerBalanceController {

    private final AccountOwnershipClient accountOwnershipClient;

    private final LedgerBalanceService balanceService;

    public LedgerBalanceController(
            AccountOwnershipClient accountOwnershipClient,
            LedgerBalanceService balanceService
    ) {
        this.accountOwnershipClient =
                Objects.requireNonNull(
                        accountOwnershipClient
                );

        this.balanceService =
                Objects.requireNonNull(
                        balanceService
                );
    }

    @GetMapping(
            "/api/v1/accounts/{accountId}/balance"
    )
    public ResponseEntity<LedgerBalanceResult> getBalance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId
    ) {
        AccountOwnershipResponse account =
                accountOwnershipClient
                        .requireOwnedAccount(
                                jwt.getTokenValue(),
                                accountId
                        );

        LedgerBalanceResult result =
                balanceService
                        .getPostedBalance(
                                account.id(),
                                account.currency()
                        );

        return ResponseEntity.ok(
                result
        );
    }
}
