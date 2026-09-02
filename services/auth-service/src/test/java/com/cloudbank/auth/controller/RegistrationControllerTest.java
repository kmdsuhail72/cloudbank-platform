package com.cloudbank.auth.controller;

import com.cloudbank.auth.exception.EmailAlreadyRegisteredException;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.model.UserRole;
import com.cloudbank.auth.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    void shouldReturnGenericAcceptedResponseForSuccessfulRegistration()
            throws Exception {
        UUID userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        AuthUser registeredUser = mock(AuthUser.class);

        when(registeredUser.getId())
                .thenReturn(userId);

        when(registeredUser.getEmail())
                .thenReturn("user@example.com");

        when(registeredUser.getRole())
                .thenReturn(UserRole.CUSTOMER);

        when(registrationService.register(
                "user@example.com",
                "StrongPass123"
        )).thenReturn(registeredUser);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message")
                        .value(
                                "If registration can be completed, further instructions will be provided."
                        ))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());

        verify(registrationService).register(
                "user@example.com",
                "StrongPass123"
        );
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Email must be valid"));
    }

    @Test
    void shouldRejectShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must be between 8 and 72 characters"));
    }

    @Test
    void shouldReturnGenericAcceptedResponseWhenEmailAlreadyRegistered()
            throws Exception {
        when(registrationService.register(
                "user@example.com",
                "StrongPass123"
        )).thenThrow(
                new EmailAlreadyRegisteredException("user@example.com")
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message")
                        .value(
                                "If registration can be completed, further instructions will be provided."
                        ))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());
    }
}
