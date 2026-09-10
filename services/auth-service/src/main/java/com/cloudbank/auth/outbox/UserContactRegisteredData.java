package com.cloudbank.auth.outbox;

import java.util.UUID;

public record UserContactRegisteredData(
        UUID userId,
        String email
) {
}
