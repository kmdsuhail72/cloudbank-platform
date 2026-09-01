package com.cloudbank.auth.controller;

import com.cloudbank.auth.dto.RegisterRequest;
import com.cloudbank.auth.dto.RegisterResponse;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthUser registeredUser = registrationService.register(
                request.email(),
                request.password()
        );

        RegisterResponse response = new RegisterResponse(
                registeredUser.getId().toString(),
                registeredUser.getEmail(),
                registeredUser.getRole().name(),
                "Registration successful"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
