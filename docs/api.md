# MySawit Payment API Documentation

Dokumen ini mendeskripsikan kontrak API untuk service Payment.

Base path:

```text
/api/v1
```

## Authentication

Sebagian besar endpoint membutuhkan JWT:

```http
Authorization: Bearer <jwt>
```

Endpoint internal memakai API key:

```http
X-Internal-Api-Key: <internal-api-key>
```

Endpoint Xendit callback memakai callback token:

```http
x-callback-token: <xendit-callback-token>
```

## Common Response Format

Success response memakai format:

```json
{
  "status": "success",
  "message": "Message",
  "data": {},
  "timestamp": "2026-05-21T00:00:00Z"
}
```

Error response memakai format:

```json
{
  "status": "error",
  "message": "Message",
  "data": null,
  "timestamp": "2026-05-21T00:00:00Z"
}
```

Validation error mengisi `data` dengan map field error:

```json
{
  "status": "error",
  "message": "Validation failed",
  "data": {
    "amountSawitDollar": "must be less than or equal to 100000.00"
  },
  "timestamp": "2026-05-21T00:00:00Z"
}
```

Paginated response memakai `data`:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## Common Error Status

| HTTP Status | Meaning |
| --- | --- |
| `400 Bad Request` | Validation error, invalid request body, invalid query/path parameter, invalid business request |
| `401 Unauthorized` | Missing/invalid JWT or internal API key |
| `403 Forbidden` | User is authenticated but not allowed, or invalid Xendit callback token |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Resource already processed, insufficient balance, gateway reference already assigned |
| `500 Internal Server Error` | Unexpected server error |

## Common Query Rules

For paginated endpoints:

| Param | Type | Default | Notes |
| --- | --- | --- | --- |
| `page` | integer | `0` | Must be `>= 0` |
| `size` | integer | `20` | Must be `1..100` |
| `sort` | string | endpoint-specific | Format: `field,direction`; direction is `asc` or `desc` |

Date filters use `yyyy-MM-dd`. `dateTo` is inclusive at date level by querying before the next day.

## Enums

```text
UserRole: BURUH, SUPIR_TRUK, MANDOR, ADMIN
PayrollStatus: PENDING, ACCEPTED, REJECTED
ReferenceType: HARVEST, DELIVERY
TransactionType: CREDIT, DEBIT
PaymentTransactionStatus: PENDING, SUCCESS, FAILED, EXPIRED
```

## Wallet

### GET /wallets/me

Get authenticated user's wallet.

Auth: JWT, any role.

Success `200` message:

```text
Wallet retrieved successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "balance": 1000.00,
  "currency": "SawitDollar",
  "createdAt": "2026-05-21T00:00:00Z",
  "updatedAt": "2026-05-21T00:00:00Z"
}
```

### GET /wallets/me/transactions

Get authenticated user's wallet transactions.

Auth: JWT, any role.

Query params:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | integer | No | Default `0` |
| `size` | integer | No | Default `20` |
| `transactionType` | `TransactionType` | No | `CREDIT` or `DEBIT` |
| `dateFrom` | date | No | `yyyy-MM-dd` |
| `dateTo` | date | No | `yyyy-MM-dd` |
| `sort` | string | No | Default `createdAt,desc`; allowed fields: `createdAt`, `amount` |

Success `200` message:

```text
Wallet transactions retrieved successfully
```

Response `data.content[]`:

```json
{
  "id": "uuid",
  "transactionType": "CREDIT",
  "amount": 100.00,
  "balanceBefore": 900.00,
  "balanceAfter": 1000.00,
  "referenceType": "TOPUP",
  "referenceId": "uuid",
  "description": "Top-up via Xendit",
  "createdAt": "2026-05-21T00:00:00Z"
}
```

### GET /wallets/{userId}

Get wallet by user id.

Auth: JWT, `ADMIN`.

Path params:

| Param | Type |
| --- | --- |
| `userId` | UUID |

Success `200` message:

```text
Wallet retrieved successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "balance": 2000.00,
  "currency": "SawitDollar",
  "createdAt": "2026-05-21T00:00:00Z",
  "updatedAt": "2026-05-21T00:00:00Z"
}
```

## Payroll

### GET /payrolls

Get all payrolls.

Auth: JWT, `ADMIN`.

Query params:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | integer | No | Default `0` |
| `size` | integer | No | Default `20` |
| `userId` | UUID | No | Filter by payroll owner id |
| `status` | `PayrollStatus` | No | `PENDING`, `ACCEPTED`, `REJECTED` |
| `userRole` | `UserRole` | No | `BURUH`, `SUPIR_TRUK`, `MANDOR`, `ADMIN` |
| `referenceType` | `ReferenceType` | No | `HARVEST`, `DELIVERY` |
| `dateFrom` | date | No | `yyyy-MM-dd` |
| `dateTo` | date | No | `yyyy-MM-dd` |
| `sort` | string | No | Default `createdAt,desc`; allowed fields: `createdAt`, `amount`, `kilogram` |

Success `200` message:

```text
Payrolls retrieved successfully
```

Response `data.content[]`:

```json
{
  "id": "uuid",
  "user": {
    "id": "uuid",
    "role": "BURUH"
  },
  "amount": 562.61,
  "kilogram": 250.50,
  "ratePerKg": 2.50,
  "multiplier": 0.90,
  "status": "PENDING",
  "referenceType": "HARVEST",
  "referenceId": "uuid",
  "description": "Upah panen",
  "createdAt": "2026-05-21T00:00:00Z"
}
```

### GET /payrolls/me

Get authenticated worker/driver/mandor payrolls.

Auth: JWT, non-`ADMIN`.

Query params:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | integer | No | Default `0` |
| `size` | integer | No | Default `20` |
| `status` | `PayrollStatus` | No | `PENDING`, `ACCEPTED`, `REJECTED` |
| `dateFrom` | date | No | `yyyy-MM-dd` |
| `dateTo` | date | No | `yyyy-MM-dd` |
| `sort` | string | No | Default `createdAt,desc`; allowed fields: `createdAt`, `amount` |

Success `200` message:

```text
My payrolls retrieved successfully
```

Response `data.content[]`:

```json
{
  "id": "uuid",
  "amount": 562.61,
  "kilogram": 250.50,
  "ratePerKg": 2.50,
  "multiplier": 0.90,
  "status": "PENDING",
  "referenceType": "HARVEST",
  "description": "Upah panen",
  "approvedAt": null,
  "createdAt": "2026-05-21T00:00:00Z"
}
```

### GET /payrolls/{payrollId}

Get payroll detail.

Auth: JWT, `ADMIN` or payroll owner.

Path params:

| Param | Type |
| --- | --- |
| `payrollId` | UUID |

Success `200` message:

```text
Payroll detail retrieved successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "user": {
    "id": "uuid",
    "role": "BURUH"
  },
  "amount": 562.61,
  "kilogram": 250.50,
  "ratePerKg": 2.50,
  "multiplier": 0.90,
  "status": "REJECTED",
  "description": "Upah panen",
  "rejectionReason": "Data kilogram tidak sesuai",
  "referenceType": "HARVEST",
  "referenceId": "uuid",
  "approvedBy": {
    "id": "uuid"
  },
  "approvedAt": "2026-05-21T00:00:00Z",
  "createdAt": "2026-05-21T00:00:00Z",
  "updatedAt": "2026-05-21T00:00:00Z"
}
```

### PUT /payrolls/{payrollId}/accept

Accept and disburse payroll.

Auth: JWT, `ADMIN`.

Path params:

| Param | Type |
| --- | --- |
| `payrollId` | UUID |

Request body: none.

Success `200` message:

```text
Payroll accepted and disbursed successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "user": {
    "id": "uuid",
    "role": "BURUH"
  },
  "amount": 562.61,
  "status": "ACCEPTED",
  "approvedBy": {
    "id": "uuid"
  },
  "approvedAt": "2026-05-21T00:00:00Z",
  "disbursement": {
    "adminWallet": {
      "balanceBefore": 50000.00,
      "balanceAfter": 49437.39
    },
    "workerWallet": {
      "balanceBefore": 688.14,
      "balanceAfter": 1250.75
    }
  }
}
```

### PUT /payrolls/{payrollId}/reject

Reject payroll.

Auth: JWT, `ADMIN`.

Path params:

| Param | Type |
| --- | --- |
| `payrollId` | UUID |

Request body:

```json
{
  "rejectionReason": "Data kilogram tidak sesuai"
}
```

Validation:

| Field | Rule |
| --- | --- |
| `rejectionReason` | Required, not blank, minimum 10 characters |

Success `200` message:

```text
Payroll rejected
```

Response `data`:

```json
{
  "id": "uuid",
  "user": {
    "id": "uuid",
    "role": "BURUH"
  },
  "amount": 562.61,
  "status": "REJECTED",
  "rejectionReason": "Data kilogram tidak sesuai",
  "approvedBy": {
    "id": "uuid"
  },
  "approvedAt": "2026-05-21T00:00:00Z"
}
```

## Top-up

### POST /topup

Create top-up invoice.

Auth: JWT, `ADMIN`.

Request body:

```json
{
  "amountSawitDollar": 10.00
}
```

Validation:

| Field | Rule |
| --- | --- |
| `amountSawitDollar` | Required, positive, max `100000.00` |

Success `200` message:

```text
Top-up created successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "amountSawitDollar": 10.00,
  "amountIdr": 100000.00,
  "exchangeRate": "1 SD = Rp 10,000",
  "paymentGateway": "XENDIT",
  "status": "PENDING",
  "paymentUrl": "https://pay.xendit.co/invoice",
  "expiresAt": "2026-05-21T01:00:00Z",
  "createdAt": "2026-05-21T00:00:00Z"
}
```

### GET /topup

Get authenticated admin's top-up history.

Auth: JWT, `ADMIN`.

Query params:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | integer | No | Default `0` |
| `size` | integer | No | Default `20` |
| `status` | `PaymentTransactionStatus` | No | `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED` |
| `sort` | string | No | Default `createdAt,desc`; allowed fields: `createdAt`, `amountSawitDollar` |

Success `200` message:

```text
Top-up history retrieved successfully
```

Response `data.content[]`:

```json
{
  "id": "uuid",
  "amountSawitDollar": 15.00,
  "amountIdr": 150000.00,
  "paymentGateway": "XENDIT",
  "status": "SUCCESS",
  "paymentUrl": "https://pay.xendit.co/invoice",
  "expiresAt": "2026-05-21T01:00:00Z",
  "createdAt": "2026-05-21T00:00:00Z",
  "updatedAt": "2026-05-21T00:00:00Z"
}
```

### GET /topup/{topupId}

Get top-up detail.

Auth: JWT, owner `ADMIN`.

Path params:

| Param | Type |
| --- | --- |
| `topupId` | UUID |

Success `200` message:

```text
Top-up detail retrieved successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "admin": {
    "id": "uuid"
  },
  "amountSawitDollar": 20.00,
  "amountIdr": 200000.00,
  "exchangeRate": "1 SD = Rp 10,000",
  "paymentGateway": "XENDIT",
  "gatewayReferenceId": "inv-123",
  "paymentUrl": "https://pay.xendit.co/invoice",
  "expiresAt": "2026-05-21T01:00:00Z",
  "status": "SUCCESS",
  "createdAt": "2026-05-21T00:00:00Z",
  "updatedAt": "2026-05-21T00:00:00Z"
}
```

### POST /topup/callback

Handle Xendit invoice callback.

Auth: `x-callback-token` header. JWT is bypassed for this endpoint.

Headers:

| Header | Required |
| --- | --- |
| `x-callback-token` | Yes |

Request body:

```json
{
  "id": "inv-123",
  "external_id": "uuid",
  "status": "PAID",
  "amount": 100000.00,
  "paid_at": "2026-05-21T00:00:00Z"
}
```

Supported callback statuses:

```text
PAID, EXPIRED, FAILED
```

Success `200` response:

```json
{
  "status": "success"
}
```

## Wage Config

### GET /wage-configs/active

Get active wage configuration.

Auth: JWT, `ADMIN`.

Success `200` message:

```text
Active wage config retrieved successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "upahBuruhPerKg": 3.00,
  "upahSupirPerKg": 2.00,
  "upahMandorPerKg": 1.50,
  "currency": "SawitDollar",
  "isActive": true,
  "updatedBy": {
    "id": "uuid"
  },
  "effectiveFrom": "2026-05-21T00:00:00Z",
  "createdAt": "2026-05-21T00:00:00Z"
}
```

### POST /wage-configs

Create new active wage configuration.

Auth: JWT, `ADMIN`.

Request body:

```json
{
  "upahBuruhPerKg": 3.00,
  "upahSupirPerKg": 2.00,
  "upahMandorPerKg": 1.50
}
```

Validation:

| Field | Rule |
| --- | --- |
| `upahBuruhPerKg` | Required, minimum `0.01` |
| `upahSupirPerKg` | Required, minimum `0.01` |
| `upahMandorPerKg` | Required, minimum `0.01` |

Success `200` message:

```text
Wage config updated successfully
```

Response `data`:

```json
{
  "id": "uuid",
  "upahBuruhPerKg": 3.00,
  "upahSupirPerKg": 2.00,
  "upahMandorPerKg": 1.50,
  "currency": "SawitDollar",
  "isActive": true,
  "previousConfig": {
    "id": "uuid",
    "upahBuruhPerKg": 2.50,
    "upahSupirPerKg": 1.50,
    "upahMandorPerKg": 1.00,
    "deactivatedAt": "2026-05-21T00:00:00Z"
  },
  "updatedBy": {
    "id": "uuid"
  },
  "effectiveFrom": "2026-05-21T00:00:00Z",
  "createdAt": "2026-05-21T00:00:00Z"
}
```

`previousConfig` can be `null` when there was no previous active config.

### GET /wage-configs/history

Get wage configuration history.

Auth: JWT, `ADMIN`.

Query params:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | integer | No | Default `0` |
| `size` | integer | No | Default `20` |

Success `200` message:

```text
Wage config history retrieved successfully
```

Response `data.content[]`:

```json
{
  "id": "uuid",
  "upahBuruhPerKg": 3.00,
  "upahSupirPerKg": 2.00,
  "upahMandorPerKg": 1.50,
  "isActive": true,
  "updatedBy": {
    "id": "uuid"
  },
  "effectiveFrom": "2026-05-21T00:00:00Z"
}
```

## Internal API

Internal endpoints are intended for service-to-service calls. They require `X-Internal-Api-Key` and bypass JWT.

### POST /internal/payrolls

Create payroll from another service event.

Auth: internal API key.

Request body:

```json
{
  "userId": "uuid",
  "userRole": "BURUH",
  "referenceType": "HARVEST",
  "referenceId": "uuid",
  "kilogram": 250.50
}
```

Validation:

| Field | Rule |
| --- | --- |
| `userId` | Required UUID |
| `userRole` | Required `UserRole` |
| `referenceType` | Required `ReferenceType` |
| `referenceId` | Required UUID |
| `kilogram` | Required, positive |

Business rules:

| Rule |
| --- |
| `ADMIN` cannot receive payroll |
| `BURUH` payroll must use `HARVEST` reference |
| `SUPIR_TRUK` and `MANDOR` payroll must use `DELIVERY` reference |

Success `200` messages:

```text
Payroll created successfully
Payroll already exists
```

Response `data`:

```json
{
  "payrollId": "uuid",
  "alreadyProcessed": false
}
```

### POST /internal/wallets

Create wallet for a user.

Auth: internal API key.

Request body:

```json
{
  "userId": "uuid"
}
```

Validation:

| Field | Rule |
| --- | --- |
| `userId` | Required UUID |

Success `200` messages:

```text
Wallet created successfully
Wallet already exists
```

Response `data`:

```json
{
  "walletId": "uuid",
  "alreadyProcessed": false
}
```
