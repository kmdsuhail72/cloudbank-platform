CREATE TABLE cards (
    id uuid PRIMARY KEY,
    auth_user_id uuid NOT NULL,
    cardholder_name varchar(120) NOT NULL,
    type varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    masked_number varchar(32) NOT NULL,
    currency varchar(3) NOT NULL,
    expiration_month integer NOT NULL,
    expiration_year integer NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_cards_owner ON cards (auth_user_id, created_at DESC);
