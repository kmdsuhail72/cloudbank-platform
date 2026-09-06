package com.cloudbank.ledger.journal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerJournalRepository
        extends JpaRepository<LedgerJournal, UUID> {

    Optional<LedgerJournal> findByReferenceTypeAndReferenceId(
            String referenceType,
            UUID referenceId
    );
}
