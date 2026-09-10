package com.cloudbank.notification.contact;

import java.util.UUID;

public record UserContactData(
        UUID userId,
        String email
) {
}
