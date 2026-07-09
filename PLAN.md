\[Project Scaffolding]

* \[ ] KMP module structure exists (android/, ios/, server/, shared/, web/ directories)
* \[ ] .gitignore configured (standard ignores for KMP setup)
* \[ ] Compose Multiplatform setup (composable files now in shared/src/commonMain/kotlin/app)
* \[ ] Ktor server skeleton (no server.main.kt or Ktor config)
* \[ ] Docker/PostgreSQL local setup (no docker-compose.yml)
* \[ ] .env file (assumed default)

\[Data Layer]

* \[ ] PostgreSQL schema via SQLDelight (no sql files in shared)
* \[ ] Migration strategy (no Migration files found)
* \[ ] User data model (no data classes in shared)
* \[ ] Club data model (no Club.kt found)
* \[ ] Membership data model (no Membership.kt found)
* \[ ] Event/Meeting data model (no Event.kt found)
* \[ ] Attendance data model (no Attendance.kt found)

\[Server API]

* \[ ] Ktor endpoints for each entity (no routes defined)
* \[ ] CORS config (no application.conf CORS settings)
* \[ ] Auth middleware (no auth logic in server)
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

* \[ ] Docker/PostgreSQL setup (no docker-compose)
* \[ ] Ktor server (no Ktor app running)
* \[ ] Android phone testing (no emulator setup)

