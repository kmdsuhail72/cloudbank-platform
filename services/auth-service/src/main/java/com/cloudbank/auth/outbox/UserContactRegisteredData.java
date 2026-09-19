package com.cloudbank.auth.outbox;

import java.util.UUID;
import java.time.LocalDate;

public record UserContactRegisteredData(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth
) {
    public UserContactRegisteredData(UUID userId, String email) {
        this(userId, email, null, null, null, null);
    }
}
