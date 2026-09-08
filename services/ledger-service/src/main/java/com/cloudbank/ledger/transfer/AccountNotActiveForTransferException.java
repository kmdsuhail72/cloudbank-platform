package com.cloudbank.ledger.transfer;

import java.util.UUID;

public class AccountNotActiveForTransferException
        extends RuntimeException {

    public AccountNotActiveForTransferException(
            UUID accountId,
            String status
    ) {
        super(
                "Account "
                        + accountId
                        + " is not active for transfer: "
                        + status
        );
    }
}
