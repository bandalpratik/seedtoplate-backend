-- V6 was already recorded as applied against a database before fulfillment_status
-- was added to that script, so Flyway will never re-run it. Re-apply the missing
-- columns here idempotently.
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS pickup_location VARCHAR(40),
    ADD COLUMN IF NOT EXISTS payment_due_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS fulfillment_status VARCHAR(30) NOT NULL DEFAULT 'PAID';

ALTER TABLE payment_intents
    ALTER COLUMN status SET DEFAULT 'INITIATED';
