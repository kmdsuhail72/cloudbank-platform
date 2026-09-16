package com.cloudbank.ledger.dev;

import com.cloudbank.ledger.account.AccountOwnershipClient;
import com.cloudbank.ledger.account.AccountOwnershipResponse;
import com.cloudbank.ledger.journal.LedgerBalanceResult;
import com.cloudbank.ledger.journal.LedgerBalanceService;
import com.cloudbank.ledger.journal.LedgerEntryType;
import com.cloudbank.ledger.journal.LedgerJournalService;
import com.cloudbank.ledger.journal.LedgerPostingCommand;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        prefix = "cloudbank.dev-test-funding",
        name = "enabled",
        havingValue = "true"
)
public class DevTestFundingService {

    static final String REFERENCE_TYPE =
            "DEV_TEST_FUNDING";

    static final UUID DEV_TEST_CLEARING_ACCOUNT_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-00000000d001"
            );

    private final AccountOwnershipClient accountOwnershipClient;
    private final LedgerJournalService journalService;
    private final LedgerBalanceService balanceService;

    public DevTestFundingService(
            AccountOwnershipClient accountOwnershipClient,
            LedgerJournalService journalService,
            LedgerBalanceService balanceService
    ) {
        this.accountOwnershipClient =
                Objects.requireNonNull(
                        accountOwnershipClient
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
    public DevTestFundingResult fund(
            String jwtToken,
            DevTestFundingRequest request
    ) {
        if (jwtToken == null
                || jwtToken.isBlank()) {
            throw new DevTestFundingException(
                    "Authentication token is required"
            );
        }

        if (request == null) {
            throw new DevTestFundingException(
                    "Funding request is required"
            );
        }

        if (request.requestId() == null) {
            throw new DevTestFundingException(
                    "Request ID is required"
            );
        }

        if (request.accountId() == null) {
            throw new DevTestFundingException(
                    "Account ID is required"
            );
        }

        BigDecimal amount =
                requireAmount(
                        request.amount()
                );

        AccountOwnershipResponse account =
                accountOwnershipClient
                        .requireOwnedAccount(
                                jwtToken,
                                request.accountId()
                        );

        if (!"ACTIVE".equalsIgnoreCase(
                account.status()
        )) {
            throw new DevTestFundingException(
                    "Account must be ACTIVE"
            );
        }

        String currency =
                requireCurrency(
                        account.currency()
                );

        journalService.postJournal(
                REFERENCE_TYPE,
                request.requestId(),
                List.of(
                        new LedgerPostingCommand(
                                DEV_TEST_CLEARING_ACCOUNT_ID,
                                LedgerEntryType.DEBIT,
                                amount,
                                currency
                        ),
                        new LedgerPostingCommand(
                                account.id(),
                                LedgerEntryType.CREDIT,
                                amount,
                                currency
                        )
                )
        );

        LedgerBalanceResult balance =
                balanceService
                        .getPostedBalance(
                                account.id(),
                                currency
                        );

        return new DevTestFundingResult(
                request.requestId(),
                account.id(),
                amount,
                currency,
                balance.postedBalance()
        );
    }

    private static BigDecimal requireAmount(
            BigDecimal amount
    ) {
        if (amount == null) {
            throw new DevTestFundingException(
                    "Amount is required"
            );
        }

        if (amount.signum() <= 0) {
            throw new DevTestFundingException(
                    "Amount must be greater than zero"
            );
        }

        final BigDecimal normalized;

        try {
            normalized =
                    amount.setScale(
                            4,
                            RoundingMode.UNNECESSARY
                    );
        } catch (ArithmeticException exception) {
            throw new DevTestFundingException(
                    "Amount must not exceed 4 decimal places"
            );
        }

        if (normalized.precision() > 19) {
            throw new DevTestFundingException(
                    "Amount exceeds supported precision"
            );
        }

        return normalized;
    }

    private static String requireCurrency(
            String currency
    ) {
        if (currency == null
                || currency.isBlank()) {
            throw new DevTestFundingException(
                    "Account currency is required"
            );
        }

        String normalized =
                currency.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalized.length() != 3) {
            throw new DevTestFundingException(
                    "Account currency must be 3 characters"
            );
        }

        return normalized;
    }
}
