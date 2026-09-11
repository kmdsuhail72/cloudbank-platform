ALTER TABLE email_deliveries
    DROP CONSTRAINT email_deliveries_status_check;

ALTER TABLE email_deliveries
    DROP CONSTRAINT chk_email_delivery_state;

ALTER TABLE email_deliveries
    ADD CONSTRAINT email_deliveries_status_check
    CHECK (
        status IN (
            'PENDING',
            'PROCESSING',
            'SENT',
            'FAILED'
        )
    );

ALTER TABLE email_deliveries
    ADD CONSTRAINT chk_email_delivery_state
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
        OR
        (
            status = 'FAILED'
            AND claim_token IS NULL
            AND lease_until IS NULL
            AND sent_at IS NULL
            AND last_error IS NOT NULL
            AND btrim(last_error) <> ''
        )
    );
