# MHS Clubs — Delivery Plan

Status key: `[x]` implemented foundation, `[ ]` not complete, `[~]` partial or stub.

## Project foundation

- [x] Kotlin Multiplatform module layout, Compose setup, Ktor server skeleton, and local
  PostgreSQL Docker configuration.
- [x] Shared domain models and SQLDelight baseline schema.
- [x] Versioned SQL schema files, including calendar-sync V2 tables.
- [ ] Reproducible Gradle build: the configured repositories currently do not resolve
  the SQLDelight 2.0.2 Gradle plugin.

## Server and data layer

- [x] Ktor route registration, Firebase token-verification helpers, CORS, Sheets,
  shared-calendar, and CSV-export service classes.
- [~] CRUD routes: route shapes and validation scaffolding exist, but database reads and
  writes remain TODO.
- [ ] Transactional PostgreSQL repositories, connection management, pagination, rate
  limiting, and production error/observability controls.

## Clients

- [x] Compose screens for authentication, clubs, events, calendar, RSVPs,
  announcements, attendance, and administration.
- [~] Android Google Sign-In: source configuration requests `calendar.events`, but the
  Firebase/Play Services deployment wiring needs verification.
- [ ] Persisted client-to-server flows for memberships, settings, and other screen data.
- [ ] Web Firebase Authentication and Google OAuth implementation.
- [ ] iOS Firebase Authentication implementation. iOS is not in the initial
  per-student Calendar scope.

## Per-student Google Calendar

- [x] AES-GCM token cipher, V2 token/mapping schema, token-refresh abstraction,
  Calendar REST client, and sync service with insert/update/delete/RRULE logic.
- [x] Unit tests for sync cleanup, missing consent, and encryption.
- [ ] Server-side authorization-code exchange and encrypted PostgreSQL token store.
- [ ] Persisted `calendar_sync_enabled` setting and student opt-out UI.
- [ ] Post-commit, background dispatch for join, leave, and schedule edits.
- [ ] Web OAuth scope request and integration-style Calendar sync tests.

## Validation and deployment

- [~] Server tests exist but cannot run until the Gradle plugin-resolution issue is fixed.
- [ ] Database, endpoint, Android UI, Web, OAuth, and Calendar integration tests.
- [ ] Firebase/Google Cloud setup, OAuth consent verification, secret-manager setup, and
  production deployment checklist completion.

See `IMPLEMENTATION_SUMMARY.md` for the consolidated review and `HUMAN_TASKS.md` for
tasks requiring cloud-console or deployment access.
