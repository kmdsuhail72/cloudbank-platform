package com.cloudbank.auth.dto;

import com.cloudbank.auth.validation.BcryptPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @BcryptPassword
        String password,

        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Size(max = 32, message = "Phone number must be at most 32 characters")
        String phoneNumber,

        @PastOrPresent(message = "Date of birth must not be in the future")
        LocalDate dateOfBirth

) {
}
