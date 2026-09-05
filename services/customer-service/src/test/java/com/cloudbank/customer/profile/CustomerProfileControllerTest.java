package com.cloudbank.customer.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cloudbank.customer.config.SecurityConfig;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CustomerProfileController.class
)
@Import(SecurityConfig.class)
class CustomerProfileControllerTest {

    private static final UUID AUTH_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerProfileService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

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
                        .claim(
                                "role",
                                "CUSTOMER"
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
                        get(
                                "/api/v1/customers/me"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldReturnProfileForAuthenticatedSubject()
            throws Exception {

        CustomerProfile profile =
                new CustomerProfile(
                        AUTH_USER_ID
                );

        when(
                service.findByAuthUserId(
                        AUTH_USER_ID
                )
        ).thenReturn(
                Optional.of(
                        profile
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
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
                                "$.authUserId"
                        ).value(
                                AUTH_USER_ID.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(
                                "ACTIVE"
                        )
                );

        verify(
                service
        ).findByAuthUserId(
                AUTH_USER_ID
        );
    }

    @Test
    void shouldReturnNotFoundWhenProfileDoesNotExist()
            throws Exception {

        when(
                service.findByAuthUserId(
                        AUTH_USER_ID
                )
        ).thenReturn(
                Optional.empty()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isNotFound()
                );

        verify(
                service
        ).findByAuthUserId(
                AUTH_USER_ID
        );
    }

    @Test
    void shouldRequireAuthenticationForProfileCreation()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/customers/me"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldCreateProfileForAuthenticatedSubject()
            throws Exception {

        CustomerProfile profile =
                new CustomerProfile(
                        AUTH_USER_ID
                );

        when(
                service.create(
                        AUTH_USER_ID
                )
        ).thenReturn(
                profile
        );

        mockMvc.perform(
                        post(
                                "/api/v1/customers/me"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath(
                                "$.authUserId"
                        ).value(
                                AUTH_USER_ID.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(
                                "ACTIVE"
                        )
                );

        verify(
                service
        ).create(
                AUTH_USER_ID
        );
    }

    @Test
    void shouldReturnConflictWhenProfileAlreadyExists()
            throws Exception {

        when(
                service.create(
                        AUTH_USER_ID
                )
        ).thenThrow(
                new CustomerProfileAlreadyExistsException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/customers/me"
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
                                "CUSTOMER_PROFILE_ALREADY_EXISTS"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Customer profile already exists"
                        )
                );

        verify(
                service
        ).create(
                AUTH_USER_ID
        );
    }

}
