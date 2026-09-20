CREATE TABLE manifests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL REFERENCES crop_batches(id),
    zone VARCHAR(120) NOT NULL,
    route VARCHAR(200) NOT NULL,
    load_kg NUMERIC(12,2) NOT NULL,
    handoff_status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
