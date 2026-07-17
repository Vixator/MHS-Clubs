# MHS Clubs — Implementation Summary and Current Review

Last reviewed: 2026-07-16

## Discrepancies Found

- Previous summary correctly said many server routes are scaffolded, but it understated how much client behavior is still sample/mock data (`app/shared/src/commonMain/kotlin/com/precon/mhsclubs/App.kt`).
- Previous summary said Android auth was partial; code confirms this, but sign-in is not wired from the login UI (`LoginScreen` sign-in callback is empty) and depends on activity-only APIs in `AndroidAuthService`.
- Previous summary was accurate that Web and iOS auth are stubs (`JsAuthService`, `IosAuthService`, `WasmAuthService`).
- Previous summary was accurate that per-student calendar sync has core service logic and tests, but is not integrated into route/database flows.
- Previous summary correctly flagged build failure on SQLDelight plugin resolution. Running `./gradlew.bat :server:test` still fails at `app/shared/build.gradle.kts` plugin `com.squareup.sqldelight:2.0.2`.

## Claim audit of the previous summary

| Previous claim | Classification | Notes |
| --- | --- | --- |
| Shared models + SQL schema foundation is present | **Accurate** | Domain/model files and SQLDelight schema/migrations are present. |
| Ktor routes/auth helpers exist but CRUD persistence is TODO | **Accurate** | CRUD routes still return placeholders and contain TODO persistence markers. |
| Android auth is partial | **Partially true** | Auth service + Calendar scope request exist, but login UI does not trigger sign-in flow end-to-end. |
| Web and iOS auth are stubs | **Accurate** | `JsAuthService`/`IosAuthService`/`WasmAuthService` return `NotImplementedError` for sign-in. |
| Per-student sync foundation exists | **Accurate** | Service/cipher/client/tests exist. |
| Build is blocked by SQLDelight plugin resolution | **Accurate** | Verified by failed `:server:test` run. |
| Compose screens cover primary product flows | **Partially true** | Screens exist, but app behavior is mostly sample-data/placeholder actions. |
| Basic route/auth/calendar tests exist | **Accurate** | Test files exist, but integration depth is limited and currently blocked by build failure. |

## Current status (code-verified)

| Area | Status | Evidence |
| --- | --- | --- |
| Module layout | Implemented foundation | `app/`, `core/`, `server/`, `app/shared`, `app/androidApp`, `app/webApp`, `app/iosApp` |
| Root `web/`, `ios/`, `android/` dirs | Stub/empty scaffolds | Root-level directories exist but contain no source files |
| Shared domain + SQL schema | Implemented foundation | SQLDelight schema/migrations in `app/shared/src/commonMain/sqldelight/mhs_clubs/` |
| Build/test pipeline | Blocked | `./gradlew.bat :server:test` fails resolving SQLDelight Gradle plugin 2.0.2 |
| Server route surface | Partial | CRUD endpoints exist, but handlers return empty/null placeholders with TODOs (`server/src/main/kotlin/.../routes/*.kt`) |
| Firebase auth verification | Partial | Verifier/plugin exist; dev fallback accepts tokens when Firebase Admin is not initialized (`FirebaseTokenVerifier`, `Application.kt`) |
| Google Sheets sync | Partial | API read + parse implemented, DB sync still TODO (`GoogleSheetsService.syncClubs`) |
| Shared-calendar service-account integration | Implemented utility layer | `GoogleCalendarService` exists but not wired to transactional domain workflows |
| Per-student calendar sync service | Implemented foundation | `StudentCalendarSyncService`, `GoogleStudentCalendarClient`, `CalendarTokenCipher`, tests |
| Per-student calendar persistence/integration | Not implemented | No PostgreSQL `StudentCalendarSyncStore`, no auth-code exchange endpoint, no route wiring |
| Android auth | Partial | `AndroidAuthService` requests `calendar.events`, but `LoginScreen` does not invoke sign-in flow |
| Web auth | Stub | `JsAuthService.signInWithGoogle` returns `NotImplementedError` |
| iOS auth | Stub | `IosAuthService.signInWithGoogle` returns `NotImplementedError` |
| UI data integration | Partial/stub | Many screens are present, but app navigation uses hardcoded sample data and placeholder actions (`App.kt`) |

## Verified implemented components

- **Schema + migrations:** baseline schema and V2 calendar sync tables (`schema.sqm`, `migrations/V1__initial_schema.sqm`, `migrations/V2__calendar_sync.sqm`).
- **Server wiring:** Ktor application setup, CORS, auth plugin install, route registration (`server/src/main/kotlin/com/precon/mhsclubs/Application.kt`).
- **Auth primitives:** Firebase token verification and role checks (`auth/FirebaseTokenVerifier.kt`, `auth/AuthPlugin.kt`, `auth/Auth.kt`).
- **Calendar sync foundation:** encrypted token helper, Google Calendar REST client for student calendars, token refresh helper, sync service with failure isolation (`services/CalendarTokenCipher.kt`, `services/GoogleStudentCalendarClient.kt`, `services/StudentCalendarSyncService.kt`).
- **Compose UI shell:** screens for clubs/events/calendar/rsvp/attendance/auth/admin exist in `app/shared/src/commonMain/kotlin/com/precon/mhsclubs/screens/`.

## Verified partial / incomplete areas

- **All CRUD routes are scaffolded** with validation but no DB persistence (`TODO` markers across `server/.../routes/*.kt`).
- **Google Sheets "sync" endpoint is not real DB sync** (returns counts, no persistence) (`GoogleSheetsService.syncClubs`).
- **Client flows are mostly not connected to backend**; app state is sample-driven (`getSample*` helpers in `App.kt`).
- **Android auth flow is not fully integrated in UI** (activity helper exists, `LoginScreen` sign-in action is empty).
- **Per-student calendar feature is not end-to-end**: no persistent token store implementation, no route integration, no post-commit dispatch from membership/event CRUD.

## Configuration and secret reality

- `.env` includes Firebase and Google variable names, but local files for `GOOGLE_SHEETS_CREDENTIALS` and `GOOGLE_CALENDAR_CREDENTIALS` are missing.
- Local Firebase service account file path in `.env` points to an existing checked-in file (`mhs-clubs-fa5d9b477aab.json`).
- OAuth client vars for per-student refresh (`GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET`) are required by code but not present in `.env`.

## Test coverage reality

- Present: `ApplicationTest`, `StudentCalendarSyncServiceTest`, `CalendarTokenCipherTest`.
- Missing: integration tests proving DB-backed CRUD, membership/event transactions, auth-code exchange, and end-to-end calendar sync workflows.
- Current tests cannot be executed through Gradle until the SQLDelight plugin-resolution issue is fixed.
