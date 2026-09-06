package com.cloudbank.ledger.journal;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
