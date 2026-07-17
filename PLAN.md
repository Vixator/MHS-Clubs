# MHS Clubs — Delivery Plan

Status key: `[x]` implemented foundation, `[ ]` not complete, `[~]` partial or stub.

## Discrepancies Found

- Prior plan treated client screens as largely complete, but app navigation/state still relies on hardcoded sample data and placeholder actions (`app/shared/src/commonMain/kotlin/com/precon/mhsclubs/App.kt`).
- Prior plan listed "persisted `calendar_sync_enabled` setting" as entirely pending; DB schema column exists, but no server/UI wiring exists yet (partial at schema level only).
- Prior plan correctly flagged route CRUD and Gradle build issues; both remain unresolved.
- Prior plan did not explicitly track missing Android login wiring from `LoginScreen` to `AndroidAuthService`.

## Project foundation

- [x] Kotlin Multiplatform module layout, Compose setup, Ktor server skeleton, and local PostgreSQL Docker configuration.
- [x] Shared domain models and SQLDelight baseline schema.
- [x] Versioned SQL schema files, including calendar-sync V2 tables.
- [ ] Reproducible Gradle build: `./gradlew.bat :server:test` fails because `com.squareup.sqldelight` plugin `2.0.2` is not resolved.

## Server and data layer

- [x] Ktor route registration, Firebase token-verification helpers, CORS, Sheets, shared-calendar, per-student calendar, and CSV-export service classes.
- [~] CRUD routes: route shapes and validation scaffolding exist, but database reads/writes remain TODO in route handlers.
- [ ] Transactional PostgreSQL repositories and real persistence for users, clubs, memberships, events, attendance, RSVPs, and announcements.
- [ ] Wire membership/event mutations to post-commit background sync dispatch for per-student calendar updates.
- [ ] Implement authenticated authorization-code exchange endpoint for per-student Google Calendar consent.

## Clients

- [~] Compose screens for authentication, clubs, events, calendar, RSVPs, announcements, attendance, and administration are present, but app behavior is largely sample-data-driven.
- [~] Android Google Sign-In service requests `calendar.events`, but login UI flow is not wired end-to-end to launch/handle sign-in.
- [ ] Replace sample data/state in `App.kt` with real API-backed state management.
- [ ] Persisted client-to-server flows for memberships, settings, and screen data updates.
- [ ] Web Firebase Authentication and Google OAuth implementation.
- [ ] iOS Firebase Authentication implementation. iOS remains out of initial per-student calendar scope.

## Per-student Google Calendar

- [x] AES-GCM token cipher, V2 token/mapping schema, token-refresh abstraction, Calendar REST client, and sync service with insert/update/delete/RRULE logic.
- [x] Unit tests for sync cleanup, missing consent, and encryption.
- [~] `calendar_sync_enabled` exists in schema (`user_calendar_tokens`), but no route/UI wiring yet.
- [ ] PostgreSQL-backed encrypted token store implementation for `StudentCalendarSyncStore`.
- [ ] Server-side authorization-code upload/exchange integration with authenticated users.
- [ ] Persisted student settings toggle and opt-out UI flow.
- [ ] Trigger sync from committed join/leave/schedule operations only (failure-isolated background path).
- [ ] Web OAuth scope request (`calendar.events`) and integration-style calendar sync tests.

## Validation and deployment

- [~] Server tests exist but are currently blocked by SQLDelight plugin resolution.
- [ ] Database, endpoint, Android UI, Web auth, OAuth, and Calendar integration tests.
- [ ] Production safeguards: explicit CORS origins, rate limiting, monitoring/alerts, and security/privacy review.
- [ ] Human-owned cloud setup and deployment tasks tracked in `HUMAN_TASKS.md`.
