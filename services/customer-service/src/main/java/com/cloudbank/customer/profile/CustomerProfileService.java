package com.cloudbank.customer.profile;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerProfileService {

    private static final String AUTH_USER_UNIQUE_CONSTRAINT =
            "uk_customer_profiles_auth_user";

    private final CustomerProfileRepository repository;
    private final ObjectMapper objectMapper;

    public CustomerProfileService(
            CustomerProfileRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createFromRegistrationEvent(String rawMessage) {
        final UserContactEventEnvelope event;
        try {
            event = objectMapper.readValue(
                    rawMessage,
                    UserContactEventEnvelope.class
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid user registration event",
                    exception
            );
        }

        if (!"USER_CONTACT_REGISTERED".equals(event.eventType())
                || event.eventVersion() != 1
                || event.data() == null
                || event.data().userId() == null
                || !event.data().userId().equals(event.aggregateId())) {
            throw new IllegalArgumentException(
                    "Unsupported user registration event"
            );
        }

        if (repository.findByAuthUserId(event.data().userId()).isEmpty()) {
            saveProfile(new CustomerProfile(
                    event.data().userId(),
                    event.data().firstName(),
                    event.data().lastName(),
                    event.data().phoneNumber(),
                    event.data().dateOfBirth()
            ));
        }
    }

    @Transactional(readOnly = true)
    public Optional<CustomerProfile> findByAuthUserId(
            UUID authUserId
    ) {
        return repository.findByAuthUserId(
                authUserId
        );
    }

    @Transactional
    public CustomerProfile create(
            UUID authUserId
    ) {
        return saveProfile(
                new CustomerProfile(
                        authUserId
                )
        );
    }

    @Transactional
    public CustomerProfile create(
            UUID authUserId,
            CustomerProfileCreateRequest request
    ) {
        return saveProfile(
                new CustomerProfile(
                        authUserId,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.dateOfBirth()
                )
        );
    }

    @Transactional
    public Optional<CustomerProfile> update(
            UUID authUserId,
            CustomerProfileUpdateRequest request
    ) {
        return repository
                .findByAuthUserId(
                        authUserId
                )
                .map(
                        profile -> {
                            profile.replaceDetails(
                                    request.firstName(),
                                    request.lastName(),
                                    request.phoneNumber(),
                                    request.dateOfBirth()
                            );

                            return repository.saveAndFlush(
                                    profile
                            );
                        }
                );
    }

    private CustomerProfile saveProfile(
            CustomerProfile profile
    ) {
        try {
            return repository.saveAndFlush(
                    profile
            );
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateAuthUserViolation(exception)) {
                throw new CustomerProfileAlreadyExistsException();
            }

            throw exception;
        }
    }

    private boolean isDuplicateAuthUserViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && AUTH_USER_UNIQUE_CONSTRAINT.equals(
                            constraintViolation.getConstraintName()
                    )) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
