package com.cloudbank.auth.controller;

import com.cloudbank.auth.dto.LoginRequest;
import com.cloudbank.auth.dto.LoginResponse;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.service.AccessTokenService;
import com.cloudbank.auth.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;
    private final AccessTokenService accessTokenService;

    public LoginController(
            LoginService loginService,
            AccessTokenService accessTokenService
    ) {
        this.loginService = loginService;
        this.accessTokenService = accessTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthUser authUser =
                loginService.login(
                        request.email(),
                        request.password()
                );

        String accessToken =
                accessTokenService.issue(authUser);

        return ResponseEntity.ok(
                new LoginResponse(
                        accessToken,
                        "Bearer"
                )
        );
    }
}
