# MHS Clubs — Human Setup and Deployment Tasks

Last reviewed: 2026-07-16

This checklist reflects the repository's current foundation status. Do not deploy it
as a finished club-management system until the persistence and OAuth tasks below are
complete.

## 1. Restore build verification

- [ ] Configure a repository that resolves `com.squareup.sqldelight:2.0.2`, then run
  `./gradlew :server:test`.
- [ ] Resolve any compile/test failures before configuring cloud resources.
- [ ] Confirm the SQLDelight V2 migration is applied to a disposable PostgreSQL database.

## 2. Firebase and Google Cloud

- [ ] Create/select the Firebase project and enable Google sign-in.
- [ ] Create the Android OAuth client; register the app package and release/debug SHA
  fingerprints as appropriate.
- [ ] Create a server/web OAuth client for authorization-code exchange.
- [ ] Enable the Google Calendar API and Google Sheets API in the same Cloud project.
- [ ] Add `https://www.googleapis.com/auth/calendar.events` to the OAuth consent screen.
- [ ] Complete any Google verification required for the new Calendar scope.
- [ ] Restrict production OAuth redirect origins and Firebase authorized domains.

## 3. Server secrets

Set these only in the server/deployment secret manager:

```text
FIREBASE_PROJECT_ID=...
FIREBASE_SERVICE_ACCOUNT=/secure/path/firebase-service-account.json
GOOGLE_SHEETS_CREDENTIALS=/secure/path/google-sheets-service-account.json
GOOGLE_SHEETS_ID=...
GOOGLE_OAUTH_CLIENT_ID=...
GOOGLE_OAUTH_CLIENT_SECRET=...
CALENDAR_TOKEN_ENCRYPTION_KEY=<base64-encoded random 32-byte key>
```

- [ ] Generate and store the encryption key securely; do not commit it.
- [ ] Define a key-rotation/re-consent procedure before storing production OAuth tokens.
- [ ] Do not use `GOOGLE_CALENDAR_CREDENTIALS` or `GOOGLE_CALENDAR_ID` for personal
  student calendars. Those variables are reserved for the separate shared-calendar service.

## 4. Required engineering completion

- [ ] Implement server database repositories and transactional CRUD route handlers.
- [ ] Add the authenticated authorization-code upload/exchange endpoint.
- [ ] Persist encrypted calendar tokens, scopes, expiry, mapping IDs, and the opt-out flag.
- [ ] Add a student settings toggle backed by `calendar_sync_enabled`.
- [ ] Run calendar sync after committed join/leave/schedule changes using a background
  job/coroutine; failures must not roll back the club operation.
- [ ] Implement Web Firebase OAuth with `calendar.events`; iOS remains deferred.
- [ ] Add integration tests for join → sync → leave and token refresh/revocation paths.

## 5. Production deployment gate

- [ ] PostgreSQL backups, migrations, and least-privilege database credentials verified.
- [ ] Firebase token verification works against the production project.
- [ ] Production CORS origins are explicit and tested.
- [ ] Calendar consent, opt-out, expired-token, and revoked-token flows tested with a
  non-production Google account.
- [ ] Monitoring/logging covers failed sync jobs without recording OAuth secrets.
- [ ] Security review covers rate limiting, authorization, and privacy/retention rules.
