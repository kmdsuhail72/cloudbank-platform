package com.cloudbank.auth.outbox;

import com.cloudbank.auth.model.AuthUser;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.time.LocalDate;

@Service
public class UserContactOutboxService {

    public static final String AGGREGATE_TYPE =
            "AUTH_USER";

    public static final String EVENT_TYPE =
            "USER_CONTACT_REGISTERED";

    public static final int EVENT_VERSION =
            1;

    private final AuthOutboxEventRepository repository;

    private final ObjectMapper objectMapper;

    public UserContactOutboxService(
            AuthOutboxEventRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository
                );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper
                );
    }

    public AuthOutboxEvent recordRegistered(
            AuthUser authUser
    ) {
        return recordRegistered(
                authUser,
                null,
                null,
                null,
                null
        );
    }

    public AuthOutboxEvent recordRegistered(
            AuthUser authUser,
            String firstName,
            String lastName,
            String phoneNumber,
            LocalDate dateOfBirth
    ) {
        Objects.requireNonNull(
                authUser,
                "Auth user is required"
        );

        if (authUser.getId() == null) {
            throw new IllegalStateException(
                    "Persisted Auth user ID is required"
            );
        }

        String payload;

        try {
            payload =
                    objectMapper.writeValueAsString(
                            new UserContactRegisteredData(
                                    authUser.getId(),
                                    authUser.getEmail(),
                                    firstName,
                                    lastName,
                                    phoneNumber,
                                    dateOfBirth
                            )
                    );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not serialize user contact event",
                    exception
            );
        }

        return repository.saveAndFlush(
                new AuthOutboxEvent(
                        AGGREGATE_TYPE,
                        authUser.getId(),
                        EVENT_TYPE,
                        EVENT_VERSION,
                        payload
                )
        );
    }
}
