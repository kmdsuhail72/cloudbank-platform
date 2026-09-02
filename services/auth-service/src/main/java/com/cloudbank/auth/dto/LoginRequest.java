package com.cloudbank.auth.dto;

import com.cloudbank.auth.validation.BcryptPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @BcryptPassword
        String password

) {
}
