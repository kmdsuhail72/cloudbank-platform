package com.cloudbank.customer.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerProfileService {

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
}
