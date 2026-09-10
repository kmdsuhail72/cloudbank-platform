package com.cloudbank.auth.outbox;

import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.repository.AuthUserRepository;
import com.cloudbank.auth.service.RegistrationService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        properties = {
                "cloudbank.outbox.publisher.enabled=false"
        }
)
class RegistrationOutboxIntegrationTest {

    private static final String EMAIL =
            "auth-outbox-test@cloudbank.local";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AuthOutboxEventRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clean();
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    @Test
    void shouldPersistUserAndContactOutboxInSameRegistration() throws Exception {
        AuthUser user =
                registrationService.register(
                        "  AUTH-OUTBOX-TEST@CLOUDBANK.LOCAL  ",
                        "StrongPass123"
                );

        assertNotNull(
                user.getId()
        );

        assertEquals(
                EMAIL,
                user.getEmail()
        );

        AuthOutboxEvent event =
                outboxRepository
                        .findByAggregateTypeAndAggregateIdAndEventType(
                                UserContactOutboxService.AGGREGATE_TYPE,
                                user.getId(),
                                UserContactOutboxService.EVENT_TYPE
                        )
                        .orElseThrow();

        assertEquals(
                UserContactOutboxService.EVENT_VERSION,
                event.getEventVersion()
        );

        assertEquals(
                1,
                outboxRepository.count()
        );

        JsonNode payload =
                objectMapper.readTree(
                        event.getPayload()
                );

        assertEquals(
                user.getId().toString(),
                payload.get("userId").asText()
        );

        assertEquals(
                EMAIL,
                payload.get("email").asText()
        );
    }

    private void clean() {
        outboxRepository.deleteAllInBatch();

        authUserRepository
                .findByEmailIgnoreCase(
                        EMAIL
                )
                .ifPresent(
                        authUserRepository::delete
                );

        authUserRepository.flush();
    }
}
