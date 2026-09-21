CREATE TABLE loans (
    id uuid PRIMARY KEY,
    auth_user_id uuid NOT NULL,
    principal_amount numeric(19,4) NOT NULL CHECK (principal_amount > 0),
    annual_interest_rate numeric(10,6) NOT NULL CHECK (annual_interest_rate >= 0),
    term_months integer NOT NULL CHECK (term_months > 0),
    currency varchar(3) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_loans_owner ON loans (auth_user_id, created_at DESC);
