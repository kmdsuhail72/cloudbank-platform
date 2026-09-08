package com.cloudbank.ledger.history;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
public class LedgerTransactionHistoryController {

    private final AccountOwnershipClient accountOwnershipClient;

    private final LedgerTransactionHistoryService historyService;

    public LedgerTransactionHistoryController(
            AccountOwnershipClient accountOwnershipClient,
            LedgerTransactionHistoryService historyService
    ) {
        this.accountOwnershipClient =
                Objects.requireNonNull(
                        accountOwnershipClient
                );

        this.historyService =
                Objects.requireNonNull(
                        historyService
                );
    }

    @GetMapping(
            "/api/v1/accounts/{accountId}/transactions"
    )
    public ResponseEntity<LedgerTransactionPage> getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @RequestParam(
                    defaultValue = "0"
            )
            int page,
            @RequestParam(
                    defaultValue = "20"
            )
            int size
    ) {
        AccountOwnershipResponse account =
                accountOwnershipClient
                        .requireOwnedAccount(
                                jwt.getTokenValue(),
                                accountId
                        );

        LedgerTransactionPage result =
                historyService
                        .getTransactions(
                                account.id(),
                                page,
                                size
                        );

        return ResponseEntity.ok(
                result
        );
    }

    @GetMapping(
            "/api/v1/accounts/{accountId}/transactions/{postingId}"
    )
    public ResponseEntity<LedgerTransactionEntry> getTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @PathVariable UUID postingId
    ) {
        AccountOwnershipResponse account =
                accountOwnershipClient
                        .requireOwnedAccount(
                                jwt.getTokenValue(),
                                accountId
                        );

        LedgerTransactionEntry result =
                historyService
                        .getTransaction(
                                account.id(),
                                postingId
                        );

        return ResponseEntity.ok(
                result
        );
    }

}