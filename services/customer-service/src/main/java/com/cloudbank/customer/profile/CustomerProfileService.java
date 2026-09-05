package com.cloudbank.customer.profile;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerProfileService {

    private static final String AUTH_USER_UNIQUE_CONSTRAINT =
            "uk_customer_profiles_auth_user";

    private final CustomerProfileRepository repository;

    public CustomerProfileService(
            CustomerProfileRepository repository
    ) {
        this.repository = repository;
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
