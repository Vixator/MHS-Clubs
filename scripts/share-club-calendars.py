#!/usr/bin/env python3
"""
Shares every club Google Calendar with the server service account as a reader
("See all event details").

Only calendars listed in the NocoDB `clubs` table are touched — personal
calendars are never modified.

Prerequisites:
  - pip install google-auth
  - GOOGLE_CALENDAR_SERVICE_ACCOUNT  → path to the service-account JSON key
  - NOCODB_BASE_URL                  → NocoDB root URL
  - NOCODB_API_TOKEN                 → NocoDB API token
  - NOCODB_CLUBS_TABLE               → NocoDB clubs table ID

Usage:
  python3 scripts/share-club-calendars.py [--dry-run]
"""

import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

from google.auth.transport.requests import Request
from google.oauth2 import service_account

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"
CALENDAR_API = "https://www.googleapis.com/calendar/v3"

REQUIRED_ENV = [
    "GOOGLE_CALENDAR_SERVICE_ACCOUNT",
    "NOCODB_BASE_URL",
    "NOCODB_API_TOKEN",
    "NOCODB_CLUBS_TABLE",
]


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def fail(msg: str) -> None:
    print(f"  ERROR: {msg}", file=sys.stderr)
    sys.exit(1)


def load_credentials() -> service_account.Credentials:
    path = os.environ["GOOGLE_CALENDAR_SERVICE_ACCOUNT"]
    if not os.path.isfile(path):
        fail(f"Service-account key file not found: {path}")
    creds = service_account.Credentials.from_service_account_file(
        path, scopes=[CALENDAR_SCOPE]
    )
    creds.refresh(Request())
    return creds


def http_get(url: str, token: str) -> dict:
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {token}"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode())


def http_post(url: str, token: str, body: dict) -> tuple[int, dict | None]:
    data = json.dumps(body).encode()
    req = urllib.request.Request(
        url,
        data=data,
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode() if exc.fp else ""
        try:
            err = json.loads(raw) if raw else {}
        except json.JSONDecodeError:
            err = {"raw": raw}
        return exc.code, err


# ---------------------------------------------------------------------------
# Core logic
# ---------------------------------------------------------------------------

def fetch_club_calendar_ids(noco_token: str) -> list[str]:
    """Return the non-empty `Calendar` values from every club record."""
    base = os.environ["NOCODB_BASE_URL"].rstrip("/")
    table = os.environ["NOCODB_CLUBS_TABLE"]
    headers = {"xc-token": noco_token, "Accept": "application/json"}
    calendar_ids = []
    offset = 0
    while True:
        url = f"{base}/api/v2/tables/{table}/records?limit=100&offset={offset}"
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = json.loads(resp.read().decode())
        records = body.get("list") or body.get("data") or []
        for rec in records:
            cal_id = rec.get("Calendar") or rec.get("calendar")
            if cal_id and cal_id.strip():
                calendar_ids.append(cal_id.strip())
        if len(records) < 100:
            break
        offset += 100
    return calendar_ids


def share_calendar(calendar_id: str, token: str, service_account_email: str, dry_run: bool) -> None:
    """Add (or confirm) the service-account reader ACL on one calendar."""
    acl_url = f"{CALENDAR_API}/calendars/{urllib.parse.quote(calendar_id)}/acl"
    acl_body = {
        "role": "reader",
        "scope": {
            "type": "user",
            "value": service_account_email,
        },
    }

    if dry_run:
        print(f"  [dry-run] would share {calendar_id} with {service_account_email} (reader)")
        return

    status, result = http_post(acl_url, token, acl_body)
    if status == 200 or status == 409:
        # 200 = created/confirmed, 409 = already exists (idempotent)
        print(f"  ✓ {calendar_id}  —  shared with {SERVICE_ACCOUNT_EMAIL} (reader)")
    else:
        err = result.get("error", {}).get("message", "unknown error") if result else "no response"
        print(f"  ✗ {calendar_id}  —  HTTP {status}: {err}")


def main() -> None:
    dry_run = "--dry-run" in sys.argv

    # --- validate environment -------------------------------------------------
    missing = [v for v in REQUIRED_ENV if not os.environ.get(v)]
    if missing:
        fail(f"Missing environment variables: {', '.join(missing)}")

    print(f"Mode: {'DRY RUN' if dry_run else 'LIVE'}")
    print()

    # --- authenticate ---------------------------------------------------------
    creds = load_credentials()
    token = creds.token
    service_account_email = creds.service_account_email
    print(f"Authenticated with service account {service_account_email}.\n")

    # --- fetch club calendars from NocoDB ------------------------------------
    noco_token = os.environ["NOCODB_API_TOKEN"]
    calendar_ids = fetch_club_calendar_ids(noco_token)

    if not calendar_ids:
        print("No club calendars found in NocoDB. Nothing to do.")
        return

    print(f"Found {len(calendar_ids)} club calendar(s) in NocoDB:\n")
    for cid in calendar_ids:
        share_calendar(cid, token, service_account_email, dry_run)

    print(f"\nDone. {len(calendar_ids)} calendar(s) processed.")


if __name__ == "__main__":
    main()
