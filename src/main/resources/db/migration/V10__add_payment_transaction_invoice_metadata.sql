ALTER TABLE payment_transactions
    ADD COLUMN payment_url VARCHAR(2048),
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;
