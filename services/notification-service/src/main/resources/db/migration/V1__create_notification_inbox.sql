CREATE TABLE inbox_events (
    event_id UUID PRIMARY KEY,

    event_type VARCHAR(80) NOT NULL
        CHECK (btrim(event_type) <> ''),

    event_version INTEGER NOT NULL
        CHECK (event_version > 0),

    aggregate_type VARCHAR(40) NOT NULL
        CHECK (btrim(aggregate_type) <> ''),

    aggregate_id UUID NOT NULL,

    occurred_at TIMESTAMPTZ NOT NULL,

    payload TEXT NOT NULL
        CHECK (btrim(payload) <> ''),

    received_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    processed_at TIMESTAMPTZ NULL
);

CREATE INDEX ix_inbox_events_processing
    ON inbox_events (
        processed_at,
        received_at
    );


CREATE TABLE transfer_notifications (
    id UUID PRIMARY KEY,

    event_id UUID NOT NULL,

    transfer_request_id UUID NOT NULL,

    journal_id UUID NOT NULL,

    source_account_id UUID NOT NULL,

    destination_account_id UUID NOT NULL,

    amount NUMERIC(19,4) NOT NULL
        CHECK (amount > 0),

    currency VARCHAR(3) NOT NULL
        CHECK (currency ~ '^[A-Z]{3}$'),

    posted_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_transfer_notifications_event
        UNIQUE (event_id),

    CONSTRAINT fk_transfer_notifications_event
        FOREIGN KEY (event_id)
        REFERENCES inbox_events(event_id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_transfer_notifications_request
    ON transfer_notifications (
        transfer_request_id
    );
