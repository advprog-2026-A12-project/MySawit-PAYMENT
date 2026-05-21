\set ON_ERROR_STOP on

BEGIN;

-- Reset only fixed CI records so the script is rerunnable.
DELETE FROM wallet_transactions
WHERE reference_id IN (
  '50000000-0000-0000-0000-00000000c001'::uuid
);

DELETE FROM payment_transactions
WHERE id = '50000000-0000-0000-0000-00000000c001'::uuid
   OR gateway_reference_id = 'inv-ci-paid-001';

-- Clean payrolls created by previous CI runs if the same DB is reused.
DELETE FROM payrolls
WHERE user_id IN (
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a006'::uuid
)
AND description LIKE '%Postman%';

-- Ensure wallets exist and admin has enough balance for payroll accept tests.
INSERT INTO wallets (id, user_id, balance, created_at, updated_at)
VALUES (
  '10000000-0000-0000-0000-00000000c001'::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'::uuid,
  1000000.00,
  now(),
  now()
)
ON CONFLICT (user_id) DO UPDATE
SET balance = 1000000.00,
    updated_at = now();

INSERT INTO wallets (id, user_id, balance, created_at, updated_at)
VALUES (
  '10000000-0000-0000-0000-00000000c002'::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a006'::uuid,
  0.00,
  now(),
  now()
)
ON CONFLICT (user_id) DO UPDATE
SET balance = 0.00,
    updated_at = now();

-- Ensure exactly one active wage config exists.
UPDATE wage_configs SET is_active = false WHERE is_active = true;

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
  '20000000-0000-0000-0000-00000000c001'::uuid,
  3.00,
  2.00,
  1.50,
  true,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'::uuid,
  now(),
  now()
)
ON CONFLICT (id) DO UPDATE
SET upah_buruh_per_kg = 3.00,
    upah_supir_per_kg = 2.00,
    upah_mandor_per_kg = 1.50,
    is_active = true,
    updated_by = 'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'::uuid,
    effective_from = now();

-- Seed one pending top-up to avoid calling the external Xendit invoice API in CI.
-- The CI Postman collection uses GET /topup/{{topupId}} first, then callback PAID.
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
VALUES (
  '50000000-0000-0000-0000-00000000c001'::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005'::uuid,
  10.00,
  100000.00,
  'XENDIT',
  'inv-ci-paid-001',
  NULL,
  'PENDING',
  now(),
  now(),
  'https://checkout-staging.xendit.co/web/ci-seeded-topup',
  now() + interval '1 hour'
);

COMMIT;

SELECT 'wallets' AS table_name, COUNT(*) AS rows FROM wallets
UNION ALL SELECT 'wage_configs', COUNT(*) FROM wage_configs
UNION ALL SELECT 'payrolls', COUNT(*) FROM payrolls
UNION ALL SELECT 'payment_transactions', COUNT(*) FROM payment_transactions
UNION ALL SELECT 'wallet_transactions', COUNT(*) FROM wallet_transactions
ORDER BY table_name;
