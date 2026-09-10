CREATE TABLE auth_outbox_events (
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(40) NOT NULL
        CHECK (btrim(aggregate_type) <> ''),

    aggregate_id UUID NOT NULL,

    event_type VARCHAR(80) NOT NULL
        CHECK (btrim(event_type) <> ''),

    event_version INTEGER NOT NULL
        CHECK (event_version > 0),

    payload TEXT NOT NULL
        CHECK (btrim(payload) <> ''),

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    published_at TIMESTAMPTZ NULL,

    attempt_count INTEGER NOT NULL
        DEFAULT 0
        CHECK (attempt_count >= 0),

    last_error TEXT NULL,

    CONSTRAINT uk_auth_outbox_aggregate_event
        UNIQUE (
            aggregate_type,
            aggregate_id,
            event_type
        )
);

CREATE INDEX ix_auth_outbox_pending
    ON auth_outbox_events (
        created_at,
        id
    )
    WHERE published_at IS NULL;
