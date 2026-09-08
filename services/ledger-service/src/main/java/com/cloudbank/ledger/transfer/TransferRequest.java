package com.cloudbank.ledger.transfer;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID requestId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {

    public TransferCommand toCommand() {
        if (requestId == null) {
            throw new InvalidTransferException(
                    "Request ID is required"
            );
        }

        if (sourceAccountId == null) {
            throw new InvalidTransferException(
                    "Source account ID is required"
            );
        }

        if (destinationAccountId == null) {
            throw new InvalidTransferException(
                    "Destination account ID is required"
            );
        }

        return new TransferCommand(
                requestId,
                sourceAccountId,
                destinationAccountId,
                amount
        );
    }
}
