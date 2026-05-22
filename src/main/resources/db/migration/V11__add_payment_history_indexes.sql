CREATE INDEX IF NOT EXISTS idx_pt_admin_created_desc
    ON payment_transactions(admin_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_pt_admin_status_created_desc
    ON payment_transactions(admin_id, status, created_at DESC);
