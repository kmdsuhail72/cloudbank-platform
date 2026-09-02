package com.cloudbank.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCredentials(
                        new InvalidCredentialsException()
                );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals(
                "INVALID_CREDENTIALS",
                response.getBody().get("error")
        );
        assertEquals(
                "Invalid email or password",
                response.getBody().get("message")
        );
    }

    @Test
    void shouldReturnGenericAcceptedResponseForAlreadyRegisteredEmail() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.handleEmailAlreadyRegistered(
                        new EmailAlreadyRegisteredException(
                                "user@example.com"
                        )
                );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals(
                "If registration can be completed, further instructions will be provided.",
                response.getBody().get("message")
        );
        assertEquals(1, response.getBody().size());
    }

}
