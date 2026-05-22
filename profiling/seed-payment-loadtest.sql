\set ON_ERROR_STOP on

-- Token claims provided by tester
\set admin_id 'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'
\set buruh_id 'dfedfa3b-eff2-49a4-bd8b-a69925c0a006'

-- Stable wallet ids to make EXPLAIN reproducible
\set admin_wallet_id '10000000-0000-0000-0000-000000000001'
\set buruh_wallet_id '10000000-0000-0000-0000-000000000002'

-- Default data volume. Override using psql -v if needed.
\if :{?wallet_tx_count}
\else
  \set wallet_tx_count 200000
\endif

\if :{?payroll_count}
\else
  \set payroll_count 200000
\endif

\if :{?topup_count}
\else
  \set topup_count 100000
\endif

\echo '== MySawit Payment load-test seed =='
\echo 'admin_id        = ' :admin_id
\echo 'buruh_id        = ' :buruh_id
\echo 'wallet_tx_count = ' :wallet_tx_count
\echo 'payroll_count   = ' :payroll_count
\echo 'topup_count     = ' :topup_count

CREATE EXTENSION IF NOT EXISTS pgcrypto;

SET client_min_messages = warning;
SET synchronous_commit = off;
SET work_mem = '256MB';

BEGIN;

-- Empty all payment tables first. CASCADE handles wallet_transactions -> wallets FK.
TRUNCATE TABLE
    wallet_transactions,
    payment_transactions,
    payrolls,
    wage_configs,
    wallets
RESTART IDENTITY CASCADE;

-- Seed wallets matching JWT subject claims.
INSERT INTO wallets (id, user_id, balance, created_at, updated_at)
VALUES
    (:'admin_wallet_id'::uuid, :'admin_id'::uuid, 1000000000.00, now() - interval '365 days', now()),
    (:'buruh_wallet_id'::uuid, :'buruh_id'::uuid, 0.00, now() - interval '365 days', now());

-- Seed active wage config.
INSERT INTO wage_configs (
    id,
    upah_buruh_per_kg,
    upah_supir_per_kg,
    upah_mandor_per_kg,
    is_active,
    updated_by,
    effective_from,
    created_at
)
VALUES (
    '20000000-0000-0000-0000-000000000001'::uuid,
    3.00,
    2.00,
    1.50,
    true,
    :'admin_id'::uuid,
    now() - interval '365 days',
    now() - interval '365 days'
);

-- Seed wallet transaction history for /wallets/me/transactions.
-- Most rows belong to the Buruh wallet so query by wallet_id + created_at DESC becomes heavy without composite index.
WITH generated AS (
    SELECT
        gs,
        CASE WHEN gs % 5 = 0 THEN 'DEBIT' ELSE 'CREDIT' END AS transaction_type,
        round((10 + (random() * 90))::numeric, 2) AS amount,
        now() - ((:wallet_tx_count - gs) * interval '1 second') AS created_at
    FROM generate_series(1, :wallet_tx_count) AS gs
), calculated AS (
    SELECT
        gs,
        transaction_type,
        amount,
        created_at,
        CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE -amount END AS delta,
        100000.00
          + SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE -amount END)
            OVER (ORDER BY gs ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS balance_after
    FROM generated
)
INSERT INTO wallet_transactions (
    id,
    wallet_id,
    transaction_type,
    amount,
    balance_before,
    balance_after,
    reference_type,
    reference_id,
    description,
    created_at
)
SELECT
    gen_random_uuid(),
    :'buruh_wallet_id'::uuid,
    transaction_type,
    amount,
    (balance_after - delta)::numeric(15,2),
    balance_after::numeric(15,2),
    CASE
        WHEN transaction_type = 'CREDIT' THEN 'PAYROLL_DISBURSEMENT'
        ELSE 'PAYROLL_DEDUCTION'
    END,
    gen_random_uuid(),
    'Load test wallet transaction #' || gs,
    created_at
FROM calculated;

-- Keep wallet.balance roughly consistent with latest history row.
UPDATE wallets w
SET balance = latest.balance_after,
    updated_at = now()
FROM (
    SELECT balance_after
    FROM wallet_transactions
    WHERE wallet_id = :'buruh_wallet_id'::uuid
    ORDER BY created_at DESC
    LIMIT 1
) latest
WHERE w.id = :'buruh_wallet_id'::uuid;

-- Seed payroll history for /payrolls/me and /payrolls.
INSERT INTO payrolls (
    id,
    user_id,
    user_role,
    amount,
    kilogram,
    rate_per_kg,
    multiplier,
    status,
    description,
    rejection_reason,
    reference_type,
    reference_id,
    approved_by,
    approved_at,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    :'buruh_id'::uuid,
    'BURUH',
    round(((50 + (random() * 350)) * 3.00 * 0.90)::numeric, 2) AS amount,
    round((50 + (random() * 350))::numeric, 2) AS kilogram,
    3.00 AS rate_per_kg,
    0.90 AS multiplier,
    CASE
        WHEN gs % 10 = 0 THEN 'REJECTED'
        WHEN gs % 3 = 0 THEN 'PENDING'
        ELSE 'ACCEPTED'
    END AS status,
    'Load test payroll BURUH #' || gs AS description,
    CASE WHEN gs % 10 = 0 THEN 'Load test rejection reason' ELSE NULL END AS rejection_reason,
    'HARVEST' AS reference_type,
    gen_random_uuid() AS reference_id,
    CASE WHEN gs % 3 = 0 THEN NULL ELSE :'admin_id'::uuid END AS approved_by,
    CASE WHEN gs % 3 = 0 THEN NULL ELSE now() - ((:payroll_count - gs) * interval '1 second') END AS approved_at,
    now() - ((:payroll_count - gs) * interval '1 second') AS created_at,
    now() - ((:payroll_count - gs) * interval '1 second') AS updated_at
FROM generate_series(1, :payroll_count) AS gs;

-- Seed top-up history for /topup.
INSERT INTO payment_transactions (
    id,
    admin_id,
    amount_sawit_dollar,
    amount_idr,
    payment_gateway,
    gateway_reference_id,
    gateway_callback_payload,
    status,
    created_at,
    updated_at,
    payment_url,
    expires_at
)
SELECT
    gen_random_uuid(),
    :'admin_id'::uuid,
    round((10 + (random() * 990))::numeric, 2) AS amount_sawit_dollar,
    round(((10 + (random() * 990)) * 10000)::numeric, 2) AS amount_idr,
    'XENDIT' AS payment_gateway,
    'loadtest-inv-' || gs || '-' || substr(md5(gs::text), 1, 12) AS gateway_reference_id,
    jsonb_build_object(
        'source', 'loadtest-seed',
        'sequence', gs,
        'status', CASE
            WHEN gs % 13 = 0 THEN 'FAILED'
            WHEN gs % 11 = 0 THEN 'EXPIRED'
            WHEN gs % 3 = 0 THEN 'PENDING'
            ELSE 'PAID'
        END
    ) AS gateway_callback_payload,
    CASE
        WHEN gs % 13 = 0 THEN 'FAILED'
        WHEN gs % 11 = 0 THEN 'EXPIRED'
        WHEN gs % 3 = 0 THEN 'PENDING'
        ELSE 'SUCCESS'
    END AS status,
    now() - ((:topup_count - gs) * interval '1 second') AS created_at,
    now() - ((:topup_count - gs) * interval '1 second') AS updated_at,
    'https://pay.xendit.co/loadtest-' || gs AS payment_url,
    now() - ((:topup_count - gs) * interval '1 second') + interval '1 hour' AS expires_at
FROM generate_series(1, :topup_count) AS gs;

COMMIT;

ANALYZE wallets;
ANALYZE wage_configs;
ANALYZE wallet_transactions;
ANALYZE payrolls;
ANALYZE payment_transactions;

\echo '== Seed completed =='
\echo 'Counts:'
SELECT 'wallets' AS table_name, count(*) AS rows FROM wallets
UNION ALL SELECT 'wage_configs', count(*) FROM wage_configs
UNION ALL SELECT 'wallet_transactions', count(*) FROM wallet_transactions
UNION ALL SELECT 'payrolls', count(*) FROM payrolls
UNION ALL SELECT 'payment_transactions', count(*) FROM payment_transactions
ORDER BY table_name;
