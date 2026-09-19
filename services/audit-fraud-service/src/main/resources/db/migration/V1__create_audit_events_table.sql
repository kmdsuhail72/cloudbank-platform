CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload JSONB NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_audit_events_event_id UNIQUE (event_id)
);

CREATE INDEX ix_audit_events_aggregate
    ON audit_events (aggregate_type, aggregate_id, occurred_at DESC);

CREATE INDEX ix_audit_events_type
    ON audit_events (event_type, occurred_at DESC);
