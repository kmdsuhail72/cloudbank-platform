CREATE TABLE beneficiaries (
    id uuid PRIMARY KEY,
    auth_user_id uuid NOT NULL,
    nickname varchar(80) NOT NULL,
    account_holder_name varchar(120) NOT NULL,
    account_number varchar(40) NOT NULL,
    bank_name varchar(120),
    currency varchar(3) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_beneficiaries_owner ON beneficiaries (auth_user_id, created_at DESC);
