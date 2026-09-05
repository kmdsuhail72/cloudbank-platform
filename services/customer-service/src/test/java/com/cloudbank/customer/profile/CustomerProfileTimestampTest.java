package com.cloudbank.customer.profile;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerProfileTimestampTest {

    @Test
    void shouldUseMicrosecondPrecisionWhenCreated() {
        CustomerProfile profile =
                new CustomerProfile(
                        UUID.randomUUID()
                );

        profile.onCreate();

        assertNotNull(
                profile.getCreatedAt()
        );

        assertNotNull(
                profile.getUpdatedAt()
        );

        assertEquals(
                0,
                profile.getCreatedAt()
                        .getNano()
                        % 1_000
        );

        assertEquals(
                0,
                profile.getUpdatedAt()
                        .getNano()
                        % 1_000
        );
    }

    @Test
    void shouldKeepCreatedAtWhenUpdated() {
        CustomerProfile profile =
                new CustomerProfile(
                        UUID.randomUUID()
                );

        profile.onCreate();

        Instant createdAt =
                profile.getCreatedAt();

        profile.onUpdate();

        assertEquals(
                createdAt,
                profile.getCreatedAt()
        );

        assertEquals(
                0,
                profile.getUpdatedAt()
                        .getNano()
                        % 1_000
        );
    }
}
