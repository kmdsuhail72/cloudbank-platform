package com.cloudbank.customer.profile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class CustomerProfileExceptionHandler {

    @ExceptionHandler(CustomerProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleProfileAlreadyExists(
            CustomerProfileAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "error",
                                "CUSTOMER_PROFILE_ALREADY_EXISTS",
                                "message",
                                exception.getMessage()
                        )
                );
    }
}
