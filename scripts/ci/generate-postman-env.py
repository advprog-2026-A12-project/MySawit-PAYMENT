#!/usr/bin/env python3
"""Generate Postman environment for CI from env vars and generated JWT files."""
import json
import os
from pathlib import Path

BASE_URL = os.environ.get("PAYMENT_CI_BASE_URL", "http://localhost:8002/api/v1")
ADMIN_TOKEN = os.environ.get("ADMIN_TOKEN") or Path("build/ci/admin.jwt").read_text().strip()
BURUH_TOKEN = os.environ.get("BURUH_TOKEN") or Path("build/ci/buruh.jwt").read_text().strip()
INTERNAL_API_KEY = os.environ["INTERNAL_API_KEY"]
CALLBACK_TOKEN = os.environ["XENDIT_WEBHOOK_TOKEN"]

values = {
    "baseUrl": BASE_URL,
    "adminToken": ADMIN_TOKEN,
    "buruhToken": BURUH_TOKEN,
    "internalApiKey": INTERNAL_API_KEY,
    "callbackToken": CALLBACK_TOKEN,
    "adminId": os.environ.get("PAYMENT_CI_ADMIN_ID", "dfedfa3b-eff2-49a4-bd8b-a69925c0a005"),
    "buruhId": os.environ.get("PAYMENT_CI_BURUH_ID", "dfedfa3b-eff2-49a4-bd8b-a69925c0a006"),
    "collectionRunId": "ci",
    "topupId": os.environ.get("PAYMENT_CI_TOPUP_ID", "50000000-0000-0000-0000-00000000c001"),
    "callbackGatewayId": os.environ.get("PAYMENT_CI_CALLBACK_GATEWAY_ID", "inv-ci-paid-001"),
}

# Keep all other collection variables present but blank.
for key in [
    "adminWalletId",
    "buruhWalletId",
    "wageConfigId",
    "acceptReferenceId",
    "acceptPayrollId",
    "rejectReferenceId",
    "rejectPayrollId",
    "shortRejectReferenceId",
    "shortRejectPayrollId",
    "badRuleReferenceId",
    "topupAmountIdr",
    "invalidTopupId",
]:
    values.setdefault(key, "")

env = {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "MySawit Payment CI",
    "values": [
        {"key": key, "value": value, "type": "default", "enabled": True}
        for key, value in values.items()
    ],
    "_postman_variable_scope": "environment",
}

out = Path("build/ci/MySawit-Payment.ci.postman_environment.json")
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(env, indent=2), encoding="utf-8")
print(f"Wrote {out}")
