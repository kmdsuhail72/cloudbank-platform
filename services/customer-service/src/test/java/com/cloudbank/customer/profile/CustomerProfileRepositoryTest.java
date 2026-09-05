package com.cloudbank.customer.profile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CustomerProfileRepositoryTest {

    @Autowired
    private CustomerProfileRepository repository;

    @Test
    void shouldPersistAndFindProfileByAuthUserId() {
        UUID authUserId = UUID.randomUUID();

        CustomerProfile profile =
                new CustomerProfile(
                        authUserId
                );

        CustomerProfile saved =
                repository.saveAndFlush(
                        profile
                );

        assertNotNull(
                saved.getId()
        );

        assertEquals(
                CustomerStatus.ACTIVE,
                saved.getStatus()
        );

        assertNotNull(
                saved.getCreatedAt()
        );

        assertNotNull(
                saved.getUpdatedAt()
        );

        assertTrue(
                repository.findByAuthUserId(
                        authUserId
                ).isPresent()
        );

        assertTrue(
                repository.existsByAuthUserId(
                        authUserId
                )
        );
    }
}
