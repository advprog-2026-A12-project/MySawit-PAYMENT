\set ON_ERROR_STOP on

SELECT
  'PAYROLL' AS test,
  id,
  amount,
  status,
  approved_by,
  approved_at
FROM payrolls
WHERE id = '30000000-0000-0000-0000-000000099001';

SELECT
  'PAYROLL_TX_COUNT' AS test,
  COUNT(*) AS count
FROM wallet_transactions
WHERE reference_id = '30000000-0000-0000-0000-000000099001';

SELECT
  'CALLBACK' AS test,
  id,
  amount_sawit_dollar,
  amount_idr,
  status,
  gateway_reference_id
FROM payment_transactions
WHERE id = '50000000-0000-0000-0000-000000099001';

SELECT
  'CALLBACK_TX_COUNT' AS test,
  COUNT(*) AS count
FROM wallet_transactions
WHERE reference_id = '50000000-0000-0000-0000-000000099001';

SELECT
  'INTERNAL_DUPLICATE_COUNT' AS test,
  COUNT(*) AS count
FROM payrolls
WHERE user_id = 'dfedfa3b-eff2-49a4-bd8b-a69925c0a006'
  AND reference_type = 'HARVEST'
  AND reference_id = '41000000-0000-0000-0000-000000099001';

SELECT
  'WALLET' AS test,
  user_id,
  balance
FROM wallets
WHERE user_id IN (
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a005',
  'dfedfa3b-eff2-49a4-bd8b-a69925c0a006'
)
ORDER BY user_id;