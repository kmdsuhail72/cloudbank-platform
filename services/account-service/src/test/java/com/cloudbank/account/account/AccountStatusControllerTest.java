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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountStatusController.class
)
@Import({
        SecurityConfig.class,
        AccountExceptionHandler.class
})
class AccountStatusControllerTest {

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
    private AccountStatusService statusService;

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
    void shouldRequireAuthentication()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "FROZEN"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldFreezeOwnedAccount()
            throws Exception {

        Account account =
                new Account(
                        CUSTOMER_ID,
                        "CB11111111111111111111111111111111",
                        AccountType.SAVINGS,
                        "INR"
                );

        account.changeCustomerManagedStatus(
                AccountStatus.FROZEN
        );

        account.onCreate();

        when(
                statusService
                        .updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.FROZEN
                        )
        ).thenReturn(
                account
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "FROZEN"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(
                                "FROZEN"
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
    }

    @Test
    void shouldRejectCustomerCloseRequest()
            throws Exception {

        when(
                statusService
                        .updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.CLOSED
                        )
        ).thenThrow(
                new AccountStatusChangeNotAllowedException()
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "CLOSED"
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
                                "ACCOUNT_STATUS_CHANGE_NOT_ALLOWED"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Requested account status change is not allowed"
                        )
                );
    }

    @Test
    void shouldHideMissingOrUnownedAccount()
            throws Exception {

        when(
                statusService
                        .updateCurrentCustomerAccountStatus(
                                "valid-token",
                                ACCOUNT_ID,
                                AccountStatus.FROZEN
                        )
        ).thenThrow(
                new AccountNotFoundException()
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "FROZEN"
                                        }
                                        """
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
                );
    }

    @Test
    void shouldRejectMissingStatus()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        "{}"
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
                                "$.fieldErrors.status"
                        ).value(
                                "Account status is required"
                        )
                );
    }

    @Test
    void shouldRejectUnknownStatusValue()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "PAUSED"
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
                                "$.fieldErrors.status"
                        ).value(
                                "Account status must be ACTIVE, FROZEN, or CLOSED"
                        )
                );
    }

    @Test
    void shouldRejectMalformedStatusJson()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{accountId}/status",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "FROZEN"
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

    @Test
    void shouldRejectMalformedAccountIdOnStatusUpdate()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/not-a-uuid/status"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "status": "FROZEN"
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
                                "INVALID_ACCOUNT_ID"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Account ID must be a valid UUID"
                        )
                );
    }

}
