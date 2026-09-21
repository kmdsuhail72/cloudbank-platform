CREATE TABLE transactions (
    id uuid PRIMARY KEY,
    auth_user_id uuid NOT NULL,
    account_id uuid NOT NULL,
    counterparty_account_id uuid,
    amount numeric(19,4) NOT NULL CHECK (amount > 0),
    currency varchar(3) NOT NULL,
    description varchar(255),
    type varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_transactions_owner ON transactions (auth_user_id, created_at DESC);
