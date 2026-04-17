CREATE TABLE payment_transactions(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL,
    amount_sawit_dollar DECIMAL(15,2) NOT NULL CHECK (amount_sawit_dollar > 0),
    amount_idr DECIMAL(15,2) NOT NULL CHECK (amount_idr > 0),
    payment_gateway VARCHAR(30) NOT NULL,
    gateway_reference_id VARCHAR(255) UNIQUE,
    gateway_callback_payload JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
     CHECK(status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_pt_admin ON payment_transactions(admin_id);
CREATE UNIQUE INDEX idx_pt_gateway_ref
    ON payment_transactions(gateway_reference_id)
    WHERE gateway_reference_id IS NOT NULL;
CREATE INDEX idx_pt_status ON payment_transactions(status);