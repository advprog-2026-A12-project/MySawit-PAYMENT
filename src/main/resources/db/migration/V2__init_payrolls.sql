CREATE TABLE payrolls (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    user_role VARCHAR(20) NOT NULL,

    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),

    kilogram DECIMAL(10,2) NOT NULL,
    rate_per_kg DECIMAL(12,2) NOT NULL,

    multiplier DECIMAL(4,2) NOT NULL DEFAULT 0.90,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
      CHECK (status IN ('PENDING','ACCEPTED','REJECTED')),

    description TEXT NOT NULL,

    rejection_reason TEXT,

    reference_type VARCHAR(30) NOT NULL
      CHECK (reference_type IN ('HARVEST','DELIVERY')),

    reference_id UUID NOT NULL,

    approved_by UUID,

    approved_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);