package com.cloudbank.customer.profile;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerProfileCreateRequest(

        @Size(
                max = 100,
                message = "First name must be at most 100 characters"
        )
        String firstName,

        @Size(
                max = 100,
                message = "Last name must be at most 100 characters"
        )
        String lastName,

        @Size(
                max = 32,
                message = "Phone number must be at most 32 characters"
        )
        String phoneNumber,

        @PastOrPresent(
                message = "Date of birth must not be in the future"
        )
        LocalDate dateOfBirth
) {
}
