# MHS Clubs

Kotlin Multiplatform club directory for Android, iOS, and Web, with a Ktor API.

## Current state

The API is a Docker-deployable, server-only adapter for Firebase, NocoDB, and read-only Google Calendar sync. It has no PostgreSQL, SQLite, SQLDelight, or database container. Web and Android load authenticated events and announcements; Web club browse/detail/join screens still use sample data and are a known engineering gap.

The Kotlin/JS dev server is on port 8081 and serves the processed Web resources correctly. Run `./gradlew.bat :app:webApp:jsBrowserDevelopmentRun`, then open `http://localhost:8081/`.

## Architecture

Firebase Google sign-in chooses an account, then Ktor verifies the Firebase ID token and enforces `STUDENT_EMAIL_DOMAIN` and `STAFF_EMAIL_DOMAIN`. Clients call Ktor only; Ktor holds the NocoDB API token. NocoDB `clubs.Id` is the canonical ID stored in every related `club_id` field.

A server service account reads each `clubs.Calendar` Google Calendar and mirrors events to NocoDB. Students and teachers do not authorize Calendar access. The Calendar scope is read-only.

## Local configuration

The server automatically finds a `.env` file by walking upward from its working directory. This means repository-root `.env` works with `./gradlew.bat :server:run`. System environment variables take precedence. Relative `FIREBASE_SERVICE_ACCOUNT` and `GOOGLE_CALENDAR_SERVICE_ACCOUNT` paths resolve relative to that `.env` file; absolute paths also work.

```text
FIREBASE_PROJECT_ID
FIREBASE_SERVICE_ACCOUNT=./firebase-service-account.json
GOOGLE_CALENDAR_SERVICE_ACCOUNT=./calendar-reader-service-account.json
STUDENT_EMAIL_DOMAIN=students.mcpasd.k12.wi.us
STAFF_EMAIL_DOMAIN=mcpasd.k12.wi.us
PORT=8080
WEB_ALLOWED_HOST=clubs.example.org
NOCODB_BASE_URL=https://your-nocodb.example.com
NOCODB_API_TOKEN=...
NOCODB_CLUBS_TABLE=...
NOCODB_USERS_TABLE=...
NOCODB_MEMBERSHIPS_TABLE=...
NOCODB_EVENTS_TABLE=...
NOCODB_RSVPS_TABLE=...
NOCODB_ATTENDANCE_TABLE=...
NOCODB_ANNOUNCEMENTS_TABLE=...
FORM_INGEST_SECRET=...
```

`WEB_ALLOWED_HOST` accepts a hostname such as `clubs.example.org` or a leading-wildcard hostname such as `*.example.org`; leave it blank for local-only use. Localhost is allowed for both HTTP and HTTPS. Never commit service-account JSON or the NocoDB token.

## NocoDB contract

The exact table/column/enumeration contract is in [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md#nocodb-schema-contract). It is important that `club_id` is the `clubs.Id` value and that select values retain their exact case/spelling.

## Commands

- Server tests: `./gradlew.bat :server:test --offline`
- Server: `./gradlew.bat :server:run`
- Web dev server: `./gradlew.bat :app:webApp:jsBrowserDevelopmentRun`
- Android build: `./gradlew.bat :app:androidApp:assembleDebug --offline`
- Build server image: `docker build -f Dockerfile.server -t mhs-clubs-server .`
- Run server image: `docker run --rm -p 8080:8080 --env-file .env -v /absolute/path/firebase.json:/run/secrets/firebase.json:ro -v /absolute/path/calendar.json:/run/secrets/calendar.json:ro -e FIREBASE_SERVICE_ACCOUNT=/run/secrets/firebase.json -e GOOGLE_CALENDAR_SERVICE_ACCOUNT=/run/secrets/calendar.json mhs-clubs-server`

## Deployment handoff

Owner-only credential, cloud setup, platform, and deployment work is in [HUMAN_TASKS.md](HUMAN_TASKS.md). Known code gaps are separate in [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md#known-code-gaps).
