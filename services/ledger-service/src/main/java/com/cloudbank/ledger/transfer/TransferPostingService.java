package com.cloudbank.ledger.transfer;

import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerBalanceResult;
import com.cloudbank.ledger.journal.LedgerBalanceService;
import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournal;
import com.cloudbank.ledger.journal.LedgerJournalRepository;
import com.cloudbank.ledger.journal.LedgerJournalResult;
import com.cloudbank.ledger.journal.LedgerJournalService;
import com.cloudbank.ledger.journal.LedgerPosting;
import com.cloudbank.ledger.journal.LedgerPostingCommand;
import com.cloudbank.ledger.journal.LedgerPostingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransferPostingService {

    private static final String REFERENCE_TYPE =
            "TRANSFER";

    private final LedgerAdvisoryLockService lockService;

    private final LedgerJournalRepository journalRepository;

    private final LedgerPostingRepository postingRepository;

    private final LedgerJournalService journalService;

    private final LedgerBalanceService balanceService;

    public TransferPostingService(
            LedgerAdvisoryLockService lockService,
            LedgerJournalRepository journalRepository,
            LedgerPostingRepository postingRepository,
            LedgerJournalService journalService,
            LedgerBalanceService balanceService
    ) {
        this.lockService =
                Objects.requireNonNull(
                        lockService
                );

        this.journalRepository =
                Objects.requireNonNull(
                        journalRepository
                );

        this.postingRepository =
                Objects.requireNonNull(
                        postingRepository
                );

        this.journalService =
                Objects.requireNonNull(
                        journalService
                );

        this.balanceService =
                Objects.requireNonNull(
                        balanceService
                );
    }

    @Transactional
    public TransferResult post(
            TransferCommand command,
            AccountOwnershipResponse source,
            AccountOwnershipResponse destination
    ) {
        Objects.requireNonNull(
                command
        );

        Objects.requireNonNull(
                source
        );

        Objects.requireNonNull(
                destination
        );

        requireExpectedAccount(
                command.sourceAccountId(),
                source
        );

        requireExpectedAccount(
                command.destinationAccountId(),
                destination
        );

        /*
         * The request lock makes retries race-safe across
         * multiple application instances sharing PostgreSQL.
         */
        lockService.lockTransferRequest(
                command.requestId()
        );

        Optional<LedgerJournal> existing =
                journalRepository
                        .findByReferenceTypeAndReferenceId(
                                REFERENCE_TYPE,
                                command.requestId()
                        );

        if (existing.isPresent()) {
            return existingTransfer(
                    command,
                    source,
                    destination,
                    existing.get()
            );
        }

        /*
         * Serialize balance check + posting for a source account.
         * This prevents two concurrent withdrawals from both
         * spending the same posted funds.
         */
        lockService.lockSourceAccount(
                command.sourceAccountId()
        );

        requireActive(
                source
        );

        requireActive(
                destination
        );

        String sourceCurrency =
                normalizeCurrency(
                        source.currency()
                );

        String destinationCurrency =
                normalizeCurrency(
                        destination.currency()
                );

        if (!sourceCurrency.equals(
                destinationCurrency
        )) {
            throw new TransferCurrencyMismatchException();
        }

        LedgerBalanceResult sourceBalance =
                balanceService
                        .getPostedBalance(
                                source.id(),
                                sourceCurrency
                        );

        if (sourceBalance
                .postedBalance()
                .compareTo(
                        command.amount()
                ) < 0) {

            throw new InsufficientFundsException();
        }

        LedgerJournalResult journal =
                journalService.postJournal(
                        REFERENCE_TYPE,
                        command.requestId(),
                        List.of(
                                new LedgerPostingCommand(
                                        source.id(),
                                        LedgerEntryType.DEBIT,
                                        command.amount(),
                                        sourceCurrency
                                ),
                                new LedgerPostingCommand(
                                        destination.id(),
                                        LedgerEntryType.CREDIT,
                                        command.amount(),
                                        sourceCurrency
                                )
                        )
                );

        return new TransferResult(
                command.requestId(),
                journal.journalId(),
                source.id(),
                destination.id(),
                command.amount(),
                sourceCurrency,
                journal.createdAt()
        );
    }

    private TransferResult existingTransfer(
            TransferCommand command,
            AccountOwnershipResponse source,
            AccountOwnershipResponse destination,
            LedgerJournal journal
    ) {
        String sourceCurrency =
                normalizeCurrency(
                        source.currency()
                );

        String destinationCurrency =
                normalizeCurrency(
                        destination.currency()
                );

        List<LedgerPosting> postings =
                postingRepository
                        .findByJournalIdOrderByCreatedAtAsc(
                                journal.getId()
                        );

        boolean debitMatches =
                postings
                        .stream()
                        .anyMatch(
                                posting ->
                                        matches(
                                                posting,
                                                source.id(),
                                                LedgerEntryType.DEBIT,
                                                command.amount(),
                                                sourceCurrency
                                        )
                        );

        boolean creditMatches =
                postings
                        .stream()
                        .anyMatch(
                                posting ->
                                        matches(
                                                posting,
                                                destination.id(),
                                                LedgerEntryType.CREDIT,
                                                command.amount(),
                                                destinationCurrency
                                        )
                        );

        if (postings.size() != 2
                || !sourceCurrency.equals(
                        destinationCurrency
                )
                || !debitMatches
                || !creditMatches) {

            throw new TransferIdempotencyConflictException();
        }

        return new TransferResult(
                command.requestId(),
                journal.getId(),
                source.id(),
                destination.id(),
                command.amount(),
                sourceCurrency,
                journal.getCreatedAt()
        );
    }

    private static boolean matches(
            LedgerPosting posting,
            java.util.UUID accountId,
            LedgerEntryType entryType,
            BigDecimal amount,
            String currency
    ) {
        return posting
                .getAccountId()
                .equals(
                        accountId
                )
                && posting.getEntryType()
                == entryType
                && posting
                .getAmount()
                .compareTo(
                        amount
                ) == 0
                && posting
                .getCurrency()
                .equals(
                        currency
                );
    }

    private static void requireExpectedAccount(
            java.util.UUID requestedAccountId,
            AccountOwnershipResponse account
    ) {
        if (!requestedAccountId.equals(
                account.id()
        )) {
            throw new InvalidTransferException(
                    "Verified account does not match requested account"
            );
        }
    }

    private static void requireActive(
            AccountOwnershipResponse account
    ) {
        if (!"ACTIVE".equals(
                account.status()
        )) {
            throw new AccountNotActiveForTransferException(
                    account.id(),
                    account.status()
            );
        }
    }

    private static String normalizeCurrency(
            String currency
    ) {
        return currency
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }
}
