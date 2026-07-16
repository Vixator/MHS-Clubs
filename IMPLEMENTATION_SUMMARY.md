# MHS Clubs — Implementation Summary and Current Review

Last reviewed: 2026-07-16

This is the canonical implementation and review document. It replaces the former
`REVIEW.md` to avoid two documents reporting conflicting project status.

## Current status

The repository contains a Kotlin Multiplatform application shell, shared domain
models, SQLDelight schema files, Compose screens, and a Ktor server skeleton. It is
not production-ready: most API route handlers are validation/response scaffolds and
do not yet persist or retrieve application data.

| Area | Status | Notes |
| --- | --- | --- |
| Shared models and SQL schema | Foundation present | Baseline schema plus calendar-sync V2 migration exist. |
| Ktor routes and Firebase auth plugin | Foundation present | Routes are registered and auth helpers exist; CRUD persistence is still TODO. |
| Android auth | Partial | Google Sign-In configuration requests `calendar.events`; the app module still needs its Firebase/Play Services wiring verified. |
| Web and iOS auth | Stub | No functional Firebase OAuth flow is implemented. |
| Shared-calendar integration | Existing separate service | Uses `GOOGLE_CALENDAR_CREDENTIALS`; it is not suitable for a student's personal calendar. |
| Per-student calendar sync | Server foundation present | Requires persistence and route/auth integration before enabling. |
| Automated verification | Blocked | Gradle configuration currently cannot resolve the SQLDelight 2.0.2 plugin. |

## Implemented components

- KMP project layout with Android, iOS, Web, shared, core, and server modules.
- Domain models for users, clubs, memberships, events, attendance, RSVPs, and announcements.
- SQLDelight baseline schema and versioned migrations.
- Ktor authentication helpers, route registration, Firebase configuration, Google Sheets,
  shared-calendar, and CSV-export service classes.
- Compose UI screens for the primary product flows. These screens are not proof that
  their backing APIs are fully implemented.
- Basic Ktor route/auth tests, plus calendar-sync service and encryption tests.

## Per-student Google Calendar sync

The new implementation is deliberately separate from the service-account calendar
service. It is designed to call each consenting student's `primary` Google Calendar.

### Included foundation

- Android requests `https://www.googleapis.com/auth/calendar.events`.
- V2 introduces `user_calendar_tokens` and `student_calendar_event_mappings`.
- `CalendarTokenCipher` encrypts OAuth secrets using AES-256-GCM.
- `GoogleStudentCalendarClient` performs `events.insert`, `events.update`, and
  `events.delete` with the student's bearer token and supports RRULE recurrence.
- `StudentCalendarSyncService` supports join, leave, and schedule-change operations;
  refreshes expired grants; skips users without consent; and logs Calendar failures
  without failing the club operation.
- Unit coverage exercises join → update → leave cleanup, consent no-op behavior, and
  token encryption.

### Required before production enablement

1. Implement a PostgreSQL-backed `StudentCalendarSyncStore` that encrypts token columns
   with `CalendarTokenCipher` and persists the opt-out preference.
2. Add an authenticated server endpoint that accepts the short-lived authorization code
   after Firebase sign-in, exchanges it server-side, and never stores it on the client.
3. Implement real user, membership, club, and schedule persistence in the route layer.
4. Dispatch `syncClubJoin`, `syncClubLeave`, and `syncClubScheduleChange` only after the
   related database transaction commits, via a background coroutine or job queue.
5. Add a persisted student settings toggle for `calendar_sync_enabled`.
6. Implement Web Firebase Google OAuth and request the same scope. iOS is explicitly
   out of scope for this calendar feature until its current stub is implemented.

## Configuration and security

Existing `GOOGLE_CALENDAR_CREDENTIALS` and `GOOGLE_CALENDAR_ID` belong to the
shared-calendar integration. Do not use them for student calendars.

The per-student service will require these server-only secrets when its production
store and authorization-code exchange are connected:

- `GOOGLE_OAUTH_CLIENT_ID`
- `GOOGLE_OAUTH_CLIENT_SECRET`
- `CALENDAR_TOKEN_ENCRYPTION_KEY` — a base64-encoded random 32-byte AES key

Enable the Google Calendar API and request `calendar.events` on the Google OAuth
consent screen. Scope changes may require Google verification. Keep OAuth client
secrets and the encryption key in the deployment secret manager, never in the app or
repository.

## Review findings

### Primary delivery risks

- Route handlers contain TODO persistence operations, so joins, leaves, schedules, and
  user settings cannot yet drive the server services.
- The build is blocked before compilation because the configured repositories do not
  resolve `com.squareup.sqldelight:2.0.2`.
- Android, Web, and iOS integrations are incomplete or stubbed; browser/OAuth behavior
  must be validated on the deployed origins.
- There is no database integration test suite, production connection pooling, rate
  limiting, pagination, or background-job infrastructure.

### Recommended order of work

1. Restore a reproducible Gradle build and run the existing tests.
2. Implement database repositories and replace route TODOs with transactional CRUD.
3. Complete the Android OAuth handoff and Web Firebase OAuth flow.
4. Connect the per-student Calendar store, job dispatch, opt-out UI, and integration
   tests using a Calendar API test double.
5. Add operational safeguards: structured logs, retry/backoff, rate limiting, metrics,
   and production CORS configuration.

See `HUMAN_TASKS.md` for setup actions that need cloud-console credentials or deployment
access.
