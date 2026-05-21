#!/usr/bin/env python3
"""Generate JWTs for Payment CI tests without external dependencies."""
import base64
import hashlib
import hmac
import json
import os
import time
from pathlib import Path

SECRET = os.environ.get("PAYMENT_CI_JWT_SECRET") or os.environ.get("JWT_SECRET")
if not SECRET:
    raise SystemExit("Missing PAYMENT_CI_JWT_SECRET or JWT_SECRET")
try:
    SIGNING_KEY = base64.b64decode(SECRET, validate=True)
except ValueError as exc:
    raise SystemExit("PAYMENT_CI_JWT_SECRET/JWT_SECRET must be Base64 encoded") from exc

ADMIN_ID = os.environ.get("PAYMENT_CI_ADMIN_ID", "dfedfa3b-eff2-49a4-bd8b-a69925c0a005")
BURUH_ID = os.environ.get("PAYMENT_CI_BURUH_ID", "dfedfa3b-eff2-49a4-bd8b-a69925c0a006")
TTL_SECONDS = int(os.environ.get("PAYMENT_CI_JWT_TTL_SECONDS", str(60 * 60 * 24)))


def select_jjwt_hmac_algorithm(signing_key: bytes) -> tuple[str, str]:
    key_bits = len(signing_key) * 8
    if key_bits >= 512:
        return "HS512", "sha512"
    if key_bits >= 384:
        return "HS384", "sha384"
    if key_bits >= 256:
        return "HS256", "sha256"
    raise SystemExit("JWT secret must decode to at least 256 bits")


JWT_ALG, HASH_ALG = select_jjwt_hmac_algorithm(SIGNING_KEY)


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def sign(payload: dict) -> str:
    header = {"alg": JWT_ALG, "typ": "JWT"}
    header_b64 = b64url(json.dumps(header, separators=(",", ":")).encode())
    payload_b64 = b64url(json.dumps(payload, separators=(",", ":")).encode())
    signing_input = f"{header_b64}.{payload_b64}".encode()
    signature = hmac.new(SIGNING_KEY, signing_input, getattr(hashlib, HASH_ALG)).digest()
    return f"{header_b64}.{payload_b64}.{b64url(signature)}"


now = int(time.time())
exp = now + TTL_SECONDS
admin_token = sign({"sub": ADMIN_ID, "role": "ADMIN", "iat": now, "exp": exp})
buruh_token = sign({"sub": BURUH_ID, "role": "BURUH", "iat": now, "exp": exp})

Path("build/ci").mkdir(parents=True, exist_ok=True)
Path("build/ci/admin.jwt").write_text(admin_token)
Path("build/ci/buruh.jwt").write_text(buruh_token)

# Also write GitHub output when available.
out = os.environ.get("GITHUB_OUTPUT")
if out:
    with open(out, "a", encoding="utf-8") as f:
        f.write(f"admin_token={admin_token}\n")
        f.write(f"buruh_token={buruh_token}\n")

print("Generated CI JWTs:")
print(f"  admin sub = {ADMIN_ID}")
print(f"  buruh sub = {BURUH_ID}")
print(f"  alg       = {JWT_ALG}")
print(f"  exp       = {exp}")
