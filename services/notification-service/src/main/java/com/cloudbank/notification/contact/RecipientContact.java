package com.cloudbank.notification.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_recipient_contacts")
public class RecipientContact {

    @Id
    @Column(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private UUID userId;

    @Column(
            nullable = false,
            length = 320
    )
    private String email;

    @Column(
            name = "source_event_id",
            nullable = false,
            unique = true
    )
    private UUID sourceEventId;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected RecipientContact() {
    }

    public RecipientContact(
            UUID userId,
            String email,
            UUID sourceEventId,
            Instant updatedAt
    ) {
        this.userId =
                Objects.requireNonNull(
                        userId,
                        "User ID is required"
                );

        this.email =
                requireEmail(
                        email
                );

        this.sourceEventId =
                Objects.requireNonNull(
                        sourceEventId,
                        "Source event ID is required"
                );

        this.updatedAt =
                Objects.requireNonNull(
                        updatedAt,
                        "Updated time is required"
                );
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireEmail(
            String value
    ) {
        if (value == null
                || value.isBlank()
                || value.length() > 320) {
            throw new IllegalArgumentException(
                    "Valid recipient email is required"
            );
        }

        return value;
    }
}
