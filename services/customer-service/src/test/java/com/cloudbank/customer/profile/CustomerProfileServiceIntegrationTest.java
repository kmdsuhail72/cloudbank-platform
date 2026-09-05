package com.cloudbank.customer.profile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CustomerProfileServiceIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private CustomerProfileService service;

    @Test
    void shouldTranslateRealDatabaseDuplicateProfileConstraint() {
        UUID authUserId =
                UUID.randomUUID();

        service.create(
                authUserId
        );

        assertThrows(
                CustomerProfileAlreadyExistsException.class,
                () -> service.create(
                        authUserId
                )
        );
    }
}
