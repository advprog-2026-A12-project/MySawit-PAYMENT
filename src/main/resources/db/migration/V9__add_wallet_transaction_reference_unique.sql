CREATE UNIQUE INDEX idx_wt_reference_type_id_transaction_type_unique
    ON wallet_transactions(reference_type, reference_id, transaction_type);
