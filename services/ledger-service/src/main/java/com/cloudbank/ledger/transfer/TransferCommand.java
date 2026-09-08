package com.cloudbank.ledger.transfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public record TransferCommand(
        UUID requestId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {

    public TransferCommand {
        Objects.requireNonNull(
                requestId,
                "Request ID is required"
        );

        Objects.requireNonNull(
                sourceAccountId,
                "Source account ID is required"
        );

        Objects.requireNonNull(
                destinationAccountId,
                "Destination account ID is required"
        );

        if (sourceAccountId.equals(
                destinationAccountId
        )) {
            throw new InvalidTransferException(
                    "Source and destination accounts must differ"
            );
        }

        if (amount == null) {
            throw new InvalidTransferException(
                    "Transfer amount is required"
            );
        }

        if (amount.signum() <= 0) {
            throw new InvalidTransferException(
                    "Transfer amount must be greater than zero"
            );
        }

        try {
            amount =
                    amount.setScale(
                            4,
                            RoundingMode.UNNECESSARY
                    );
        } catch (ArithmeticException exception) {
            throw new InvalidTransferException(
                    "Transfer amount supports at most four decimal places"
            );
        }

        if (amount.precision() > 19) {
            throw new InvalidTransferException(
                    "Transfer amount exceeds supported precision"
            );
        }
    }
}
