INSERT INTO users (phone, full_name, role, otp_verified)
VALUES
    ('9000000099', 'Seed & Plate Admin', 'ADMIN', TRUE),
    ('9000000001', 'Customer One', 'CUSTOMER', TRUE)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO crop_batches (
    crop_name, farm_name, current_stage, actual_yield_kg, available_yield_kg, released_kg,
    estimated_price_low_per_kg, estimated_price_high_per_kg, description, variety
)
VALUES
    ('Basmati Rice', 'Wai Farm', 'GROWING', 620, 620, 0, 92, 108, 'Native-season rice from the Wai rows.', 'Basmati'),
    ('Rajapuri Turmeric', 'Wai Farm', 'STORED_CURING', 212, 212, 0, 205, 250, 'Cured and kept in the farm store.', 'Rajapuri'),
    ('Farm Onion', 'Wai Farm', 'BATCH_RELEASED', 1200, 198, 500, 88, 96, 'Stored onion packed in release slices.', 'Nashik Local')
ON CONFLICT DO NOTHING;
