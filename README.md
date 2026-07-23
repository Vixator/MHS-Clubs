# MHS Clubs

Kotlin Multiplatform club directory for Android, iOS, and Web, with a Ktor API.

## Architecture

Google sign-in is provided by Firebase Authentication in a personal Firebase/Google Cloud project. Firebase may show any Google account in its account picker. After Firebase verifies the ID token, the Ktor server accepts only verified school addresses:

- `@students.mcpasd.k12.wi.us` → student
- `@mcpasd.k12.wi.us` → teacher administrator

The domains are configurable through `STUDENT_EMAIL_DOMAIN` and `STAFF_EMAIL_DOMAIN`. There is no organization-owned OAuth dependency, hosted-domain Google restriction, Firebase custom-claim admin role, Calendar permission, Google Calendar API, or Google Sheets API.

NocoDB is the data backend. Clients call Ktor; only Ktor holds the NocoDB API token and calls NocoDB's REST API. Teachers administer every club and can set a student's membership `is_club_admin` flag, granting that student administration of that club only.

The app's calendar is an in-app view of club events. It never reads, writes, or requests access to a user's Google Calendar.

## Local configuration

Copy the variable names in `.env` into your local environment or deployment secret manager. Required server values are:

```text
FIREBASE_PROJECT_ID
FIREBASE_SERVICE_ACCOUNT=/absolute/path/to/firebase-service-account.json
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
```

`PORT` is supplied by most cloud hosts and defaults to `8080` locally. Set `WEB_ALLOWED_HOST` to the deployed Web app's hostname (without scheme or path) so its browser requests are permitted by CORS.

Never commit the Firebase service-account JSON or NocoDB API token. The server fails closed for protected routes if Firebase Admin is not initialized.

## Club data import

Export or prepare a UTF-8 CSV using [club-import-template.csv](data/club-import-template.csv), then import it into NocoDB's `clubs` table. A clean CSV is better than a live spreadsheet connection because the source changes annually and needs human cleanup first.

## Development commands

- Server tests: `./gradlew.bat :server:test`
- Android build: `./gradlew.bat :app:androidApp:assembleDebug`
- Server: `./gradlew.bat :server:run`

Platform setup is in [HUMAN_TASKS.md](HUMAN_TASKS.md). The server, Android, and Web compile successfully; iOS must be built on macOS after its Firebase packages are added.
