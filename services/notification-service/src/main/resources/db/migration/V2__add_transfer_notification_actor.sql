ALTER TABLE transfer_notifications
    ADD COLUMN actor_user_id UUID NULL;

CREATE INDEX ix_transfer_notifications_actor_user_id
    ON transfer_notifications (actor_user_id)
    WHERE actor_user_id IS NOT NULL;
