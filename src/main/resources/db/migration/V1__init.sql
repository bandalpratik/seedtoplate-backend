CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(10) NOT NULL UNIQUE,
    full_name VARCHAR(120),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    otp_code VARCHAR(6),
    otp_expires_at TIMESTAMPTZ,
    otp_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE crop_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crop_name VARCHAR(120) NOT NULL,
    farm_name VARCHAR(120) NOT NULL,
    current_stage VARCHAR(30) NOT NULL DEFAULT 'GROWING',
    actual_yield_kg NUMERIC(12,2) NOT NULL DEFAULT 0,
    available_yield_kg NUMERIC(12,2) NOT NULL DEFAULT 0,
    released_kg NUMERIC(12,2) NOT NULL DEFAULT 0,
    estimated_price_low_per_kg NUMERIC(12,2),
    estimated_price_high_per_kg NUMERIC(12,2),
    description VARCHAR(500),
    variety VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    batch_id UUID NOT NULL REFERENCES crop_batches(id),
    reserved_kg NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_RELEASE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    price_per_kg NUMERIC(12,2),
    expected_price_low_per_kg NUMERIC(12,2),
    expected_price_high_per_kg NUMERIC(12,2),
    cancel_reason VARCHAR(200),
    release_id UUID
);

CREATE TABLE batch_releases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL REFERENCES crop_batches(id),
    released_kg NUMERIC(12,2) NOT NULL,
    price_per_kg NUMERIC(12,2) NOT NULL,
    released_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    note VARCHAR(500)
);

CREATE TABLE payment_intents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL UNIQUE REFERENCES reservations(id),
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_ref VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_crop_batches_stage ON crop_batches(current_stage);
CREATE INDEX idx_crop_batches_farm ON crop_batches(farm_name);
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_batch ON reservations(batch_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_batch_releases_batch ON batch_releases(batch_id);
CREATE INDEX idx_batch_releases_released_at ON batch_releases(released_at);
CREATE INDEX idx_payment_intents_reservation ON payment_intents(reservation_id);
CREATE INDEX idx_payment_intents_status ON payment_intents(status);
