package com.cloudbank.customer.profile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfileResponse(

        UUID id,

        UUID authUserId,

        String firstName,

        String lastName,

        String phoneNumber,

        LocalDate dateOfBirth,

        CustomerStatus status,

        Instant createdAt,

        Instant updatedAt

) {

    public static CustomerProfileResponse from(
            CustomerProfile profile
    ) {
        return new CustomerProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
