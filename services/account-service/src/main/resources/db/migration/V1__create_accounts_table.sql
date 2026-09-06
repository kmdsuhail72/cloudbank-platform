CREATE TABLE accounts (
    id UUID PRIMARY KEY,

    customer_id UUID NOT NULL,

    account_number VARCHAR(34) NOT NULL,

    account_type VARCHAR(20) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_accounts_account_number
        UNIQUE (account_number),

    CONSTRAINT chk_accounts_account_type
        CHECK (
            account_type IN (
                'CHECKING',
                'SAVINGS'
            )
        ),

    CONSTRAINT chk_accounts_currency
        CHECK (
            currency = UPPER(currency)
            AND char_length(currency) = 3
        ),

    CONSTRAINT chk_accounts_status
        CHECK (
            status IN (
                'ACTIVE',
                'FROZEN',
                'CLOSED'
            )
        )
);

CREATE INDEX ix_accounts_customer_id
    ON accounts (customer_id);
