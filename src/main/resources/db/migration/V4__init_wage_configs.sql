CREATE TABLE wage_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    upah_buruh_per_kg DECIMAL(12, 2) NOT NULL CHECK (upah_buruh_per_kg > 0),
    upah_supir_per_kg DECIMAL(12, 2) NOT NULL CHECK (upah_supir_per_kg > 0),
    upah_mandor_per_kg DECIMAL(12, 2) NOT NULL CHECK (upah_mandor_per_kg > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by UUID NOT NULL,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_wc_active
    ON wage_configs (is_active)
    WHERE is_active = TRUE;