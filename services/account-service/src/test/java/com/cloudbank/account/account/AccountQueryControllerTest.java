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
import java.util.List;
import java.util.UUID;

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
class AccountQueryControllerTest {

    private static final UUID AUTH_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
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
    void shouldRequireAuthenticationForAccountList()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/accounts"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldReturnCurrentCustomersAccounts()
            throws Exception {

        Account savings =
                new Account(
                        CUSTOMER_ID,
                        "CB11111111111111111111111111111111",
                        AccountType.SAVINGS,
                        "inr"
                );

        savings.onCreate();

        Account checking =
                new Account(
                        CUSTOMER_ID,
                        "CB22222222222222222222222222222222",
                        AccountType.CHECKING,
                        "usd"
                );

        checking.onCreate();

        when(
                queryService.findCurrentCustomerAccounts(
                        "valid-token"
                )
        ).thenReturn(
                List.of(
                        savings,
                        checking
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts"
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
                                "$.length()"
                        ).value(
                                2
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[0].accountType"
                        ).value(
                                "SAVINGS"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[0].currency"
                        ).value(
                                "INR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[1].accountType"
                        ).value(
                                "CHECKING"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[1].currency"
                        ).value(
                                "USD"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[0].customerId"
                        ).doesNotExist()
                )
                .andExpect(
                        jsonPath(
                                "$[0].balance"
                        ).doesNotExist()
                );
    }

    @Test
    void shouldRejectAccountListWhenCustomerProfileIsMissing()
            throws Exception {

        when(
                queryService.findCurrentCustomerAccounts(
                        "valid-token"
                )
        ).thenThrow(
                new CustomerProfileRequiredException()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "CUSTOMER_PROFILE_REQUIRED"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Customer profile is required for account operations"
                        )
                );
    }
}
