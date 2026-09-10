CREATE TABLE notification_recipient_contacts (
    user_id UUID PRIMARY KEY,

    email VARCHAR(320) NOT NULL
        CHECK (btrim(email) <> ''),

    source_event_id UUID NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_notification_recipient_contacts_event
        UNIQUE (source_event_id),

    CONSTRAINT fk_notification_recipient_contacts_event
        FOREIGN KEY (source_event_id)
        REFERENCES inbox_events(event_id)
        ON DELETE RESTRICT
);
