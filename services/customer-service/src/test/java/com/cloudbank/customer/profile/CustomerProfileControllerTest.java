package com.cloudbank.customer.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

        CustomerProfileCreateRequest request =
                new CustomerProfileCreateRequest(
                        "Suhail",
                        "Ahmed",
                        "+91-9999999999",
                        java.time.LocalDate.of(
                                1998,
                                1,
                                15
                        )
                );

        CustomerProfile profile =
                new CustomerProfile(
                        AUTH_USER_ID,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.dateOfBirth()
                );

        when(
                service.create(
                        AUTH_USER_ID,
                        request
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "firstName": "Suhail",
                                          "lastName": "Ahmed",
                                          "phoneNumber": "+91-9999999999",
                                          "dateOfBirth": "1998-01-15"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath(
                                "$.firstName"
                        ).value(
                                "Suhail"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.lastName"
                        ).value(
                                "Ahmed"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.phoneNumber"
                        ).value(
                                "+91-9999999999"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.dateOfBirth"
                        ).value(
                                "1998-01-15"
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
                AUTH_USER_ID,
                request
        );
    }

    @Test
    void shouldReturnConflictWhenProfileAlreadyExists()
            throws Exception {

        CustomerProfileCreateRequest request =
                new CustomerProfileCreateRequest(
                        "Suhail",
                        "Ahmed",
                        null,
                        null
                );

        when(
                service.create(
                        AUTH_USER_ID,
                        request
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "firstName": "Suhail",
                                          "lastName": "Ahmed"
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
    }


    @Test
    void shouldRejectFutureDateOfBirth()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/customers/me"
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
                                          "firstName": "Suhail",
                                          "lastName": "Ahmed",
                                          "dateOfBirth": "2999-01-01"
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
                                "$.fieldErrors.dateOfBirth"
                        ).value(
                                "Date of birth must not be in the future"
                        )
                );
    }


    @Test
    void shouldRequireAuthenticationForProfileUpdate()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/customers/me"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldUpdateProfileForAuthenticatedSubject()
            throws Exception {

        CustomerProfileUpdateRequest request =
                new CustomerProfileUpdateRequest(
                        "Updated",
                        "Customer",
                        "+91-8888888888",
                        java.time.LocalDate.of(
                                1998,
                                1,
                                15
                        )
                );

        CustomerProfile profile =
                new CustomerProfile(
                        AUTH_USER_ID,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.dateOfBirth()
                );

        when(
                service.update(
                        AUTH_USER_ID,
                        request
                )
        ).thenReturn(
                Optional.of(
                        profile
                )
        );

        mockMvc.perform(
                        put(
                                "/api/v1/customers/me"
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
                                          "firstName": "Updated",
                                          "lastName": "Customer",
                                          "phoneNumber": "+91-8888888888",
                                          "dateOfBirth": "1998-01-15"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.firstName"
                        ).value(
                                "Updated"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.lastName"
                        ).value(
                                "Customer"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.phoneNumber"
                        ).value(
                                "+91-8888888888"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.dateOfBirth"
                        ).value(
                                "1998-01-15"
                        )
                );

        verify(
                service
        ).update(
                AUTH_USER_ID,
                request
        );
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingProfile()
            throws Exception {

        CustomerProfileUpdateRequest request =
                new CustomerProfileUpdateRequest(
                        "Updated",
                        "Customer",
                        null,
                        null
                );

        when(
                service.update(
                        AUTH_USER_ID,
                        request
                )
        ).thenReturn(
                Optional.empty()
        );

        mockMvc.perform(
                        put(
                                "/api/v1/customers/me"
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
                                          "firstName": "Updated",
                                          "lastName": "Customer"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void shouldRejectFutureDateOfBirthOnUpdate()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/customers/me"
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
                                          "firstName": "Updated",
                                          "lastName": "Customer",
                                          "dateOfBirth": "2999-01-01"
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
                                "$.fieldErrors.dateOfBirth"
                        ).value(
                                "Date of birth must not be in the future"
                        )
                );
    }



    @Test
    void shouldRejectMalformedJsonOnCreate()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/customers/me"
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
                                          "firstName": "Suhail",
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
    void shouldRejectMalformedJsonOnUpdate()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/customers/me"
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
                                          "firstName": "Updated",
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
