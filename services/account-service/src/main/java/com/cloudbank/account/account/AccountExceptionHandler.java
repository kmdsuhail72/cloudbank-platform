package com.cloudbank.account.account;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class AccountExceptionHandler {

    @ExceptionHandler(CustomerProfileRequiredException.class)
    public ResponseEntity<Map<String, String>> handleCustomerProfileRequired(
            CustomerProfileRequiredException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        Map.of(
                                "error",
                                "CUSTOMER_PROFILE_REQUIRED",
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFound(
            AccountNotFoundException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        Map.of(
                                "error",
                                "ACCOUNT_NOT_FOUND",
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(AccountStatusChangeNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleStatusChangeNotAllowed(
            AccountStatusChangeNotAllowedException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        Map.of(
                                "error",
                                "ACCOUNT_STATUS_CHANGE_NOT_ALLOWED",
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "error",
                "VALIDATION_ERROR"
        );

        response.put(
                "message",
                "Request validation failed"
        );

        response.put(
                "fieldErrors",
                fieldErrors
        );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJson(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        Map.of(
                                "error",
                                "INVALID_JSON",
                                "message",
                                "Malformed JSON request"
                        )
                );
    }
}
