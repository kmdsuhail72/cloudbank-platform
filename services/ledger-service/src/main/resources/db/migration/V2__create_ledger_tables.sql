CREATE TABLE ledger_journals (
    id UUID PRIMARY KEY,

    reference_type VARCHAR(40) NOT NULL,

    reference_id UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_ledger_journals_reference
        UNIQUE (
            reference_type,
            reference_id
        ),

    CONSTRAINT chk_ledger_journals_reference_type
        CHECK (
            char_length(
                trim(reference_type)
            ) > 0
        )
);

CREATE TABLE ledger_postings (
    id UUID PRIMARY KEY,

    journal_id UUID NOT NULL,

    account_id UUID NOT NULL,

    entry_type VARCHAR(10) NOT NULL,

    amount NUMERIC(19, 4) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_postings_journal
        FOREIGN KEY (
            journal_id
        )
        REFERENCES ledger_journals (
            id
        )
        ON DELETE RESTRICT,

    CONSTRAINT chk_ledger_postings_entry_type
        CHECK (
            entry_type IN (
                'DEBIT',
                'CREDIT'
            )
        ),

    CONSTRAINT chk_ledger_postings_amount
        CHECK (
            amount > 0
        ),

    CONSTRAINT chk_ledger_postings_currency
        CHECK (
            currency = UPPER(currency)
            AND char_length(currency) = 3
        )
);

CREATE INDEX ix_ledger_postings_journal_id
    ON ledger_postings (
        journal_id
    );

CREATE INDEX ix_ledger_postings_account_id
    ON ledger_postings (
        account_id
    );

CREATE INDEX ix_ledger_postings_account_created_at
    ON ledger_postings (
        account_id,
        created_at
    );
