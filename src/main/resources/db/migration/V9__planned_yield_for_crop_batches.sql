ALTER TABLE crop_batches
    ADD COLUMN IF NOT EXISTS planned_yield_kg NUMERIC(12,2) NOT NULL DEFAULT 0;

UPDATE crop_batches
SET planned_yield_kg = CASE
    WHEN COALESCE(planned_yield_kg, 0) > 0 THEN planned_yield_kg
    WHEN COALESCE(actual_yield_kg, 0) > 0 THEN actual_yield_kg
    ELSE COALESCE(available_yield_kg, 0)
END;
