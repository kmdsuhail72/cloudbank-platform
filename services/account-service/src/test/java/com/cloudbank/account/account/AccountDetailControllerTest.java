package com.cloudbank.account.account;

import com.cloudbank.account.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountController.class
)
@Import({
        SecurityConfig.class,
        AccountExceptionHandler.class
})
class AccountDetailControllerTest {

    private static final UUID AUTH_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountCreationService creationService;

    @MockitoBean
    private AccountQueryService queryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Instant now =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue(
                                "valid-token"
                        )
                        .header(
                                "alg",
                                "RS256"
                        )
                        .subject(
                                AUTH_USER_ID.toString()
                        )
                        .issuedAt(
                                now
                        )
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .build();

        when(
                jwtDecoder.decode(
                        "valid-token"
                )
        ).thenReturn(
                jwt
        );
    }

    @Test
    void shouldRequireAuthenticationForAccountDetail()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}",
                                ACCOUNT_ID
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldReturnCurrentCustomersAccount()
            throws Exception {

        Account account =
                new Account(
                        CUSTOMER_ID,
                        "CB33333333333333333333333333333333",
                        AccountType.SAVINGS,
                        "inr"
                );

        account.onCreate();

        when(
                queryService.findCurrentCustomerAccount(
                        "valid-token",
                        ACCOUNT_ID
                )
        ).thenReturn(
                account
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.accountNumber"
                        ).value(
                                "CB33333333333333333333333333333333"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.accountType"
                        ).value(
                                "SAVINGS"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.currency"
                        ).value(
                                "INR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(
                                "ACTIVE"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.customerId"
                        ).doesNotExist()
                )
                .andExpect(
                        jsonPath(
                                "$.balance"
                        ).doesNotExist()
                );

        verify(
                queryService
        ).findCurrentCustomerAccount(
                "valid-token",
                ACCOUNT_ID
        );
    }

    @Test
    void shouldReturnNotFoundForMissingOrUnownedAccount()
            throws Exception {

        when(
                queryService.findCurrentCustomerAccount(
                        "valid-token",
                        ACCOUNT_ID
                )
        ).thenThrow(
                new AccountNotFoundException()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "ACCOUNT_NOT_FOUND"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Account not found"
                        )
                );
    }
}
