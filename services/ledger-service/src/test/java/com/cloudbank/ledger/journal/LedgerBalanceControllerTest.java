package com.cloudbank.ledger.journal;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerBalanceControllerTest {

    @Mock
    private AccountOwnershipClient accountOwnershipClient;

    @Mock
    private LedgerBalanceService balanceService;

    private LedgerBalanceController controller;

    @BeforeEach
    void setUp() {
        controller =
                new LedgerBalanceController(
                        accountOwnershipClient,
                        balanceService
                );
    }

    @Test
    void shouldVerifyOwnershipAndUseAccountCurrency() {
        UUID accountId =
                UUID.randomUUID();

        Jwt jwt =
                mock(
                        Jwt.class
                );

        when(
                jwt.getTokenValue()
        ).thenReturn(
                "access-token"
        );

        when(
                accountOwnershipClient
                        .requireOwnedAccount(
                                "access-token",
                                accountId
                        )
        ).thenReturn(
                new AccountOwnershipResponse(
                        accountId,
                        "INR",
                        "ACTIVE"
                )
        );

        LedgerBalanceResult balance =
                new LedgerBalanceResult(
                        accountId,
                        "INR",
                        new BigDecimal(
                                "125.0000"
                        )
                );

        when(
                balanceService
                        .getPostedBalance(
                                accountId,
                                "INR"
                        )
        ).thenReturn(
                balance
        );

        ResponseEntity<LedgerBalanceResult> response =
                controller.getBalance(
                        jwt,
                        accountId
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                balance,
                response.getBody()
        );

        verify(
                accountOwnershipClient
        ).requireOwnedAccount(
                "access-token",
                accountId
        );

        verify(
                balanceService
        ).getPostedBalance(
                accountId,
                "INR"
        );
    }
}
