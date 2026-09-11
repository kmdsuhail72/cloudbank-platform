CREATE TABLE email_deliveries (
    id UUID PRIMARY KEY,

    notification_id UUID NOT NULL,

    recipient_user_id UUID NOT NULL,

    status VARCHAR(20) NOT NULL
        DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'PROCESSING',
                'SENT'
            )
        ),

    attempt_count INTEGER NOT NULL
        DEFAULT 0
        CHECK (attempt_count >= 0),

    next_attempt_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    claim_token UUID NULL,

    lease_until TIMESTAMPTZ NULL,

    last_error TEXT NULL,

    sent_at TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_email_deliveries_notification
        UNIQUE (notification_id),

    CONSTRAINT fk_email_deliveries_notification
        FOREIGN KEY (notification_id)
        REFERENCES transfer_notifications(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_email_delivery_state
        CHECK (
            (
                status = 'PENDING'
                AND claim_token IS NULL
                AND lease_until IS NULL
                AND sent_at IS NULL
            )
            OR
            (
                status = 'PROCESSING'
                AND claim_token IS NOT NULL
                AND lease_until IS NOT NULL
                AND sent_at IS NULL
            )
            OR
            (
                status = 'SENT'
                AND claim_token IS NULL
                AND lease_until IS NULL
                AND sent_at IS NOT NULL
            )
        )
);

CREATE INDEX ix_email_deliveries_pending
    ON email_deliveries (
        next_attempt_at,
        created_at,
        id
    )
    WHERE status = 'PENDING';

CREATE INDEX ix_email_deliveries_processing_lease
    ON email_deliveries (
        lease_until,
        id
    )
    WHERE status = 'PROCESSING';

CREATE INDEX ix_email_deliveries_recipient
    ON email_deliveries (
        recipient_user_id
    );
