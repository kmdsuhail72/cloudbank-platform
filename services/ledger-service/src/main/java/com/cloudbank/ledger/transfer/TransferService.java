package com.cloudbank.ledger.transfer;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountOwnershipClient accountOwnershipClient;

    private final TransferPostingService postingService;

    public TransferService(
            AccountOwnershipClient accountOwnershipClient,
            TransferPostingService postingService
    ) {
        this.accountOwnershipClient =
                Objects.requireNonNull(
                        accountOwnershipClient
                );

        this.postingService =
                Objects.requireNonNull(
                        postingService
                );
    }

    public TransferResult transfer(
            String accessToken,
            TransferCommand command,
            UUID actorUserId
    ) {
        if (accessToken == null
                || accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token is required"
            );
        }

        Objects.requireNonNull(
                command,
                "Transfer command is required"
        );

        Objects.requireNonNull(
                actorUserId,
                "Actor user ID is required"
        );

        AccountOwnershipResponse source =
                accountOwnershipClient
                        .requireOwnedAccount(
                                accessToken,
                                command.sourceAccountId()
                        );

        AccountOwnershipResponse destination =
                accountOwnershipClient
                        .requireOwnedAccount(
                                accessToken,
                                command.destinationAccountId()
                        );

        return postingService.post(
                command,
                source,
                destination,
                actorUserId
        );
    }
}
