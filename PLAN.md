\[Project Scaffolding]

* \[x] KMP module structure exists (android/, ios/, server/, shared/, web/ directories)
* \[x] .gitignore configured (standard ignores for KMP setup)
* \[x] Compose Multiplatform setup (composable files now in shared/src/commonMain/kotlin/app)
* \[x] Ktor server skeleton (no server.main.kt or Ktor config)
* \[x] Docker/PostgreSQL local setup (no docker-compose.yml)
* \[x] .env file (assumed default)

\[Data Layer]

* \[x] PostgreSQL schema via SQLDelight (8 tables: User, Club, ClubOverride, Membership, Event, Attendance, RSVP, Announcement)
* \[x] Migration strategy (versioned .sqm files with schema.sqm reference + verifyMigrations)
* \[x] User data model (User + UserRole enum in core module)
* \[x] Club data model (Club + fromSheet factory in core module)
* \[x] Membership data model (Membership.kt with MembershipRole/MembershipStatus enums)
* \[x] Event/Meeting data model (Event.kt with Google Calendar sync flag)
* \[x] Attendance data model (Attendance.kt with AttendanceStatus enum)

\[Server API]

* \[x] Ktor endpoints for each entity (7 route files with full CRUD under /api/)
* \[x] CORS config (CORS plugin allowing localhost and \*.mcpasd.k12.wi.us)
* \[x] Auth middleware (Firebase ID token verification via Ktor plugin)
* \[ ] Role-based authorization (no auth rules)

\[Authentication]

* \[ ] Firebase Google Sign-In (no Firebase setup)
* \[ ] Domain-restricted role assignment (no domain check)
* \[ ] Incremental OAuth scope (no Calendar API scope)

\[Google Sheets Integration]

* \[ ] Read-only club data import (no sheet integration)
* \[ ] Data sync (no sync logic)

\[Core Features - Android]

* \[ ] Auth screens (no LoginActivity or AccountActivity)
* \[ ] Club Directory/Search (no ClubListScreen)
* \[ ] Club Details (no ClubDetailScreen)
* \[ ] Join Club flow (no JoinFlow.kt)
* \[ ] In-app Calendar view (no CalendarView.kt)
* \[ ] Event/RSVP (no EventRSVP.kt)
* \[ ] Announcements (no AnnouncementScreen)
* \[ ] Admin dashboard (no AdminPanel)
* \[ ] Attendance tracking (no AttendanceTracker)
* \[ ] CSV export (no ExportUtil.kt)

\[Google Calendar Sync]

* \[ ] Server-side token storage (no token DB)
* \[ ] Event creation (no Calendar API calls)

\[Testing]

* \[ ] JUnit for shared/server (no tests directory)
* \[ ] Espresso for Android UI (no tests/ directory)

\[iOS and Web parity]

* \[ ] iOS main (no iOS target)
* \[ ] Web UI (no web/ main)
* \[ ] Parity check (no parity test scripts)

\[Setup Checkpoints]

* \[x] Docker/PostgreSQL setup (no docker-compose)
* \[x] Ktor server (no Ktor app running)
* \[ ] Android phone testing (no emulator setup)

