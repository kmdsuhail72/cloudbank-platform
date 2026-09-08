package com.cloudbank.ledger.journal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LedgerJournalService {

    private final LedgerJournalRepository journalRepository;
    private final LedgerPostingRepository postingRepository;

    public LedgerJournalService(
            LedgerJournalRepository journalRepository,
            LedgerPostingRepository postingRepository
    ) {
        this.journalRepository =
                Objects.requireNonNull(
                        journalRepository
                );

        this.postingRepository =
                Objects.requireNonNull(
                        postingRepository
                );
    }

    @Transactional
    public LedgerJournalResult postJournal(
            String referenceType,
            UUID referenceId,
            List<LedgerPostingCommand> commands
    ) {
        if (commands == null
                || commands.size() < 2) {
            throw new InvalidJournalException(
                    "Ledger journal requires at least two postings"
            );
        }

        LedgerJournal journal =
                new LedgerJournal(
                        referenceType,
                        referenceId
                );

        List<LedgerPosting> postings =
                buildValidatedPostings(
                        journal.getId(),
                        commands
                );

        LedgerJournal persistedJournal;

        try {
            persistedJournal =
                    journalRepository.saveAndFlush(
                            journal
                    );
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateLedgerJournalException();
        }

        postingRepository.saveAllAndFlush(
                postings
        );

        return new LedgerJournalResult(
                persistedJournal.getId(),
                persistedJournal.getReferenceType(),
                persistedJournal.getReferenceId(),
                persistedJournal.getCreatedAt(),
                postings.size()
        );
    }

    private List<LedgerPosting> buildValidatedPostings(
            UUID journalId,
            List<LedgerPostingCommand> commands
    ) {
        List<LedgerPosting> postings =
                new ArrayList<>();

        BigDecimal debitTotal =
                BigDecimal.ZERO
                        .setScale(4);

        BigDecimal creditTotal =
                BigDecimal.ZERO
                        .setScale(4);

        String journalCurrency =
                null;

        boolean hasDebit =
                false;

        boolean hasCredit =
                false;

        for (LedgerPostingCommand command : commands) {
            if (command == null) {
                throw new InvalidJournalException(
                        "Ledger posting is required"
                );
            }

            LedgerPosting posting =
                    new LedgerPosting(
                            journalId,
                            command.accountId(),
                            command.entryType(),
                            command.amount(),
                            command.currency()
                    );

            if (journalCurrency == null) {
                journalCurrency =
                        posting.getCurrency();
            } else if (!journalCurrency.equals(
                    posting.getCurrency()
            )) {
                throw new InvalidJournalException(
                        "Ledger journal postings must use one currency"
                );
            }

            if (posting.getEntryType()
                    == LedgerEntryType.DEBIT) {
                hasDebit =
                        true;

                debitTotal =
                        debitTotal.add(
                                posting.getAmount()
                        );
            } else {
                hasCredit =
                        true;

                creditTotal =
                        creditTotal.add(
                                posting.getAmount()
                        );
            }

            postings.add(
                    posting
            );
        }

        if (!hasDebit || !hasCredit) {
            throw new InvalidJournalException(
                    "Ledger journal requires both debit and credit postings"
            );
        }

        if (debitTotal.compareTo(
                creditTotal
        ) != 0) {
            throw new UnbalancedJournalException();
        }

        return List.copyOf(
                postings
        );
    }
}
