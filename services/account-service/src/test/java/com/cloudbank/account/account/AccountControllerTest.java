package com.cloudbank.account.account;

import com.cloudbank.account.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountController.class
)
@Import({
        SecurityConfig.class,
        AccountExceptionHandler.class
})
class AccountControllerTest {

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
    private AccountCreationService service;

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
    void shouldRequireAuthenticationForAccountCreation()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "accountType": "SAVINGS",
                                          "currency": "INR"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldCreateAccountForAuthenticatedCustomer()
            throws Exception {

        Account account =
                new Account(
                        CUSTOMER_ID,
                        "CB1234567890ABCDEF1234567890ABCDEF",
                        AccountType.SAVINGS,
                        "inr"
                );

        account.onCreate();

        when(
                service.create(
                        "valid-token",
                        AccountType.SAVINGS,
                        "inr"
                )
        ).thenReturn(
                account
        );

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "accountType": "SAVINGS",
                                          "currency": "inr"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath(
                                "$.id"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.accountNumber"
                        ).value(
                                "CB1234567890ABCDEF1234567890ABCDEF"
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
                service
        ).create(
                "valid-token",
                AccountType.SAVINGS,
                "inr"
        );
    }

    @Test
    void shouldRejectMissingAccountType()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "currency": "INR"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "VALIDATION_ERROR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Request validation failed"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.accountType"
                        ).value(
                                "Account type is required"
                        )
                );
    }

    @Test
    void shouldRejectInvalidCurrency()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "accountType": "CHECKING",
                                          "currency": "US1"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "VALIDATION_ERROR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.currency"
                        ).value(
                                "Currency must be exactly 3 letters"
                        )
                );
    }

    @Test
    void shouldRejectCreationWhenCustomerProfileIsMissing()
            throws Exception {

        when(
                service.create(
                        "valid-token",
                        AccountType.SAVINGS,
                        "INR"
                )
        ).thenThrow(
                new CustomerProfileRequiredException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "accountType": "SAVINGS",
                                          "currency": "INR"
                                        }
                                        """
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

    @Test
    void shouldRejectMalformedJson()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/accounts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "accountType": "SAVINGS",
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "INVALID_JSON"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Malformed JSON request"
                        )
                );
    }
}
