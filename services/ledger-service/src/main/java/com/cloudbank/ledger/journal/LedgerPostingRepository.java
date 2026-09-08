package com.cloudbank.ledger.journal;

import com.cloudbank.ledger.history.LedgerTransactionView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
                    SELECT
                        p.id AS "postingId",
                        p.journal_id AS "journalId",
                        j.reference_type AS "referenceType",
                        j.reference_id AS "referenceId",
                        p.entry_type AS "entryType",
                        p.amount AS "amount",
                        p.currency AS "currency",
                        p.created_at AS "createdAt"
                    FROM ledger_postings p
                    JOIN ledger_journals j
                      ON j.id = p.journal_id
                    WHERE p.account_id = :accountId
                    ORDER BY
                        p.created_at DESC,
                        p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM ledger_postings
                    WHERE account_id = :accountId
                    """,
            nativeQuery = true
    )
    Page<LedgerTransactionView> findTransactionHistory(
            @Param("accountId") UUID accountId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        p.id AS "postingId",
                        p.journal_id AS "journalId",
                        j.reference_type AS "referenceType",
                        j.reference_id AS "referenceId",
                        p.entry_type AS "entryType",
                        p.amount AS "amount",
                        p.currency AS "currency",
                        p.created_at AS "createdAt"
                    FROM ledger_postings p
                    JOIN ledger_journals j
                      ON j.id = p.journal_id
                    WHERE p.id = :postingId
                      AND p.account_id = :accountId
                    """,
            nativeQuery = true
    )
    Optional<LedgerTransactionView> findTransactionDetail(
            @Param("accountId") UUID accountId,
            @Param("postingId") UUID postingId
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
