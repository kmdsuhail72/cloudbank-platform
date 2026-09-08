package com.cloudbank.ledger.transfer;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.UUID;

@Component
public class LedgerAdvisoryLockService {

    private final JdbcTemplate jdbcTemplate;

    public LedgerAdvisoryLockService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(
                        jdbcTemplate
                );
    }

    public void lockTransferRequest(
            UUID requestId
    ) {
        Objects.requireNonNull(
                requestId,
                "Request ID is required"
        );

        lock(
                "transfer-request:"
                        + requestId
        );
    }

    public void lockSourceAccount(
            UUID accountId
    ) {
        Objects.requireNonNull(
                accountId,
                "Account ID is required"
        );

        lock(
                "ledger-account:"
                        + accountId
        );
    }

    private void lock(
            String lockKey
    ) {
        jdbcTemplate.execute(
                (ConnectionCallback<Void>) connection -> {
                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            """
                                            SELECT pg_advisory_xact_lock(
                                                hashtextextended(?, 0)
                                            )
                                            """
                                    )
                    ) {
                        statement.setString(
                                1,
                                lockKey
                        );

                        statement.execute();
                    }

                    return null;
                }
        );
    }
}
