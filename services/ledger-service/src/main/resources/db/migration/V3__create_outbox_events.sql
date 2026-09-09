CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id UUID NOT NULL,

    event_type VARCHAR(80) NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,

    payload TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ NULL,

    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT NULL,

    CONSTRAINT ck_outbox_events_aggregate_type_nonblank
        CHECK (char_length(btrim(aggregate_type)) > 0),

    CONSTRAINT ck_outbox_events_event_type_nonblank
        CHECK (char_length(btrim(event_type)) > 0),

    CONSTRAINT ck_outbox_events_payload_nonblank
        CHECK (char_length(btrim(payload)) > 0),

    CONSTRAINT ck_outbox_events_event_version_positive
        CHECK (event_version > 0),

    CONSTRAINT ck_outbox_events_attempt_count_nonnegative
        CHECK (attempt_count >= 0),

    CONSTRAINT uk_outbox_events_aggregate_event
        UNIQUE (
            aggregate_type,
            aggregate_id,
            event_type
        )
);

CREATE INDEX ix_outbox_events_pending
    ON outbox_events (
        created_at,
        id
    )
    WHERE published_at IS NULL;
