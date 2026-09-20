INSERT INTO users (phone, full_name, role, otp_verified)
VALUES
    ('9000000001', 'Customer One', 'CUSTOMER', TRUE),
    ('9000000099', 'Seed & Plate Admin', 'ADMIN', TRUE),
    ('9000000002', 'Customer Two', 'CUSTOMER', TRUE)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO crop_batches (
    crop_name, farm_name, current_stage, actual_yield_kg, available_yield_kg, released_kg,
    estimated_price_low_per_kg, estimated_price_high_per_kg, description, variety
)
VALUES
    ('Rajapuri Turmeric', 'Wai Farm', 'STORED_CURING', 212, 212, 0, 205, 250, 'Cured and kept in the farm store.', 'Rajapuri'),
    ('Farm Onion', 'Wai Farm', 'BATCH_RELEASED', 1200, 198, 500, 88, 96, 'Onion release in slices.', 'Nashik Local'),
    ('Basmati Rice', 'Wai Farm', 'GROWING', 620, 620, 0, 92, 108, 'Native-season rice from the Wai rows.', 'Basmati')
ON CONFLICT DO NOTHING;

INSERT INTO reservations (user_id, batch_id, reserved_kg, status, price_per_kg, expected_price_low_per_kg, expected_price_high_per_kg, created_at)
SELECT u.id, b.id, 25, 'PENDING_RELEASE', NULL, 205, 250, NOW()
FROM users u, crop_batches b
WHERE u.phone = '9000000001' AND b.crop_name = 'Rajapuri Turmeric'
ON CONFLICT DO NOTHING;

INSERT INTO reservations (user_id, batch_id, reserved_kg, status, price_per_kg, expected_price_low_per_kg, expected_price_high_per_kg, created_at)
SELECT u.id, b.id, 15, 'PENDING_RELEASE', NULL, 88, 96, NOW()
FROM users u, crop_batches b
WHERE u.phone = '9000000002' AND b.crop_name = 'Farm Onion'
ON CONFLICT DO NOTHING;

INSERT INTO batch_releases (batch_id, released_kg, price_per_kg, released_at, note)
SELECT b.id, 150, 92, NOW(), 'Second tranche'
FROM crop_batches b
WHERE b.crop_name = 'Farm Onion'
ON CONFLICT DO NOTHING;

INSERT INTO manifests (batch_id, zone, route, load_kg, handoff_status)
SELECT b.id, 'Pune', 'Wai -> Pune', 150, 'PLANNED'
FROM crop_batches b
WHERE b.crop_name = 'Farm Onion'
ON CONFLICT DO NOTHING;
