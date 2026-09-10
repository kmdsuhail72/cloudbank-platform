package com.cloudbank.ledger.transfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    private static final UUID ACTOR_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    @Mock
    private TransferService transferService;

    @Test
    void shouldSubmitAuthenticatedTransfer() {
        UUID requestId =
                UUID.randomUUID();

        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        TransferRequest request =
                new TransferRequest(
                        requestId,
                        sourceId,
                        destinationId,
                        new BigDecimal(
                                "25.0000"
                        )
                );

        TransferCommand command =
                request.toCommand();

        TransferResult result =
                new TransferResult(
                        requestId,
                        UUID.randomUUID(),
                        sourceId,
                        destinationId,
                        new BigDecimal(
                                "25.0000"
                        ),
                        "INR",
                        Instant.now()
                );

        Jwt jwt =
                mock(
                        Jwt.class
                );

        when(
                jwt.getTokenValue()
        ).thenReturn(
                "access-token"
        );

        when(
                transferService.transfer(
                        "access-token",
                        command
                )
        ).thenReturn(
                result
        );

        TransferController controller =
                new TransferController(
                        transferService
                );

        ResponseEntity<TransferResult> response =
                controller.transfer(
                        jwt,
                        request
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                result,
                response.getBody()
        );

        verify(
                transferService
        ).transfer(
                "access-token",
                command
        );
    }
}
