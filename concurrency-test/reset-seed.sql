\set ON_ERROR_STOP on

BEGIN;

DELETE FROM wallet_transactions;
DELETE FROM payrolls;
DELETE FROM payment_transactions;
DELETE FROM wage_configs;
DELETE FROM wallets;

INSERT INTO wallets (id, user_id, balance, created_at, updated_at)
VALUES
(
  md5('wallet-admin-fixed')::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005',
  100000000.00,
  now(),
  now()
),
(
  md5('wallet-buruh-fixed')::uuid,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a006',
  100000.00,
  now(),
  now()
);

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
  md5('wage-config-concurrency')::uuid,
  3.00,
  2.00,
  1.50,
  true,
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005',
  now(),
  now()
);

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
VALUES (
  '30000000-0000-0000-0000-000000099001',
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a006',
  'BURUH',
  270.00,
  100.00,
  3.00,
  0.90,
  'PENDING',
  'Concurrency test payroll accept',
  NULL,
  'HARVEST',
  '40000000-0000-0000-0000-000000099001',
  NULL,
  NULL,
  now(),
  now()
);

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
  '50000000-0000-0000-0000-000000099001',
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005',
  10.00,
  100000.00,
  'XENDIT',
  'inv-concurrency-paid-001',
  NULL,
  'PENDING',
  now(),
  now(),
  'https://checkout-staging.xendit.co/web/concurrency-test',
  now() + interval '1 hour'
);

COMMIT;

SELECT 'READY' AS status;