package com.cloudbank.ledger.transfer;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final UUID ACTOR_USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    @Mock
    private AccountOwnershipClient accountOwnershipClient;

    @Mock
    private TransferPostingService postingService;

    @Test
    void shouldResolveOwnershipForBothAccountsBeforePosting() {
        UUID requestId =
                UUID.randomUUID();

        UUID sourceId =
                UUID.randomUUID();

        UUID destinationId =
                UUID.randomUUID();

        TransferCommand command =
                new TransferCommand(
                        requestId,
                        sourceId,
                        destinationId,
                        new BigDecimal(
                                "25.0000"
                        )
                );

        AccountOwnershipResponse source =
                new AccountOwnershipResponse(
                        sourceId,
                        "INR",
                        "ACTIVE"
                );

        AccountOwnershipResponse destination =
                new AccountOwnershipResponse(
                        destinationId,
                        "INR",
                        "ACTIVE"
                );

        TransferResult expected =
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

        when(
                accountOwnershipClient
                        .requireOwnedAccount(
                                "access-token",
                                sourceId
                        )
        ).thenReturn(
                source
        );

        when(
                accountOwnershipClient
                        .requireOwnedAccount(
                                "access-token",
                                destinationId
                        )
        ).thenReturn(
                destination
        );

        when(
                postingService.post(
                        command,
                        source,
                        destination
                ,
                        ACTOR_USER_ID)
        ).thenReturn(
                expected
        );

        TransferService service =
                new TransferService(
                        accountOwnershipClient,
                        postingService
                );

        TransferResult actual =
                service.transfer(
                        "access-token",
                        command
                ,
                        ACTOR_USER_ID);

        assertEquals(
                expected,
                actual
        );

        verify(
                accountOwnershipClient
        ).requireOwnedAccount(
                "access-token",
                sourceId
        );

        verify(
                accountOwnershipClient
        ).requireOwnedAccount(
                "access-token",
                destinationId
        );
    }
}
