CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE payrolls
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE UNIQUE INDEX idx_payrolls_reference
    ON payrolls(reference_type, reference_id, user_id);

CREATE INDEX idx_payrolls_user ON payrolls(user_id);
CREATE INDEX idx_payrolls_status ON payrolls(status);
CREATE INDEX idx_payrolls_created ON payrolls(created_at);