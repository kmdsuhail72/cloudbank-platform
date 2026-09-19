package com.cloudbank.auth.controller;

import com.cloudbank.auth.dto.RegisterRequest;
import com.cloudbank.auth.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private static final String REGISTRATION_RESPONSE_MESSAGE =
            "If registration can be completed, further instructions will be provided.";

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        if (request.firstName() == null
                && request.lastName() == null
                && request.phoneNumber() == null
                && request.dateOfBirth() == null) {
            registrationService.register(
                    request.email(),
                    request.password()
            );
        } else {
            registrationService.register(
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName(),
                    request.phoneNumber(),
                    request.dateOfBirth()
            );
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "message",
                        REGISTRATION_RESPONSE_MESSAGE
                ));
    }
}
