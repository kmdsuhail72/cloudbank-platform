package com.cloudbank.customer.profile;

import java.time.LocalDate;
import java.util.UUID;

public record UserContactData(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth
) {
}
