package com.cloudbank.ledger.journal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerPostingRepository
        extends JpaRepository<LedgerPosting, UUID> {

    List<LedgerPosting> findByJournalIdOrderByCreatedAtAsc(
            UUID journalId
    );

    List<LedgerPosting> findByAccountIdOrderByCreatedAtAsc(
            UUID accountId
    );

    @Query(
            value = """
                    SELECT COALESCE(
                        SUM(
                            CASE
                                WHEN entry_type = 'CREDIT'
                                    THEN amount
                                ELSE -amount
                            END
                        ),
                        0
                    )
                    FROM ledger_postings
                    WHERE account_id = :accountId
                      AND currency = :currency
                    """,
            nativeQuery = true
    )
    BigDecimal calculatePostedBalance(
            @Param("accountId") UUID accountId,
            @Param("currency") String currency
    );
}
