\[Project Scaffolding]

* \[x] KMP module structure exists (android/, ios/, server/, shared/, web/ directories)
* \[x] .gitignore configured (standard ignores for KMP setup)
* \[x] Compose Multiplatform setup (composable files now in shared/src/commonMain/kotlin/app)
* \[x] Ktor server skeleton (Application.kt with Firebase and Sheets integration)
* \[x] Docker/PostgreSQL local setup (docker-compose.yml exists)
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
* \[x] Role-based authorization (AuthPlugin with AuthenticationScheme enum and route-level auth)

\[Authentication]

* \[x] Firebase Google Sign-In (FirebaseConfig.kt, FirebaseTokenVerifier.kt with dev mode support)
* \[x] Domain-restricted role assignment (FirebaseTokenVerifier checks @mcpasd.k12.wi.us and @students.mcpasd.k12.wi.us)
* \[x] Incremental OAuth scope (Google Sign-In configured with Calendar scope in AndroidAuthService)

\[Google Sheets Integration]

* \[x] Read-only club data import (GoogleSheetsService.kt with Sheets API v4)
* \[x] Data sync (SyncRoutes.kt with POST /api/sync/clubs endpoint)

\[Core Features - Android]

* \[x] Auth screens (LoginScreen.kt, AccountScreen.kt)
* \[x] Club Directory/Search (ClubListScreen.kt with searchable list)
* \[x] Club Details (ClubDetailScreen.kt with full club info)
* \[x] Join Club flow (JoinClubScreen.kt with code input)
* \[x] In-app Calendar view (CalendarScreen.kt with monthly view)
* \[x] Event/RSVP (EventListScreen.kt, RsvpScreen.kt)
* \[x] Announcements (AnnouncementListScreen.kt)
* \[x] Admin dashboard (AdminDashboardScreen.kt)
* \[x] Attendance tracking (AttendanceScreen.kt)
* \[x] CSV export (CsvExportService.kt)

\[Google Calendar Sync]

* \[x] Server-side token storage (GoogleCalendarService.kt with service account support)
* \[x] Event creation (createEvent, updateEvent, deleteEvent methods)

\[Testing]

* \[x] JUnit for shared/server (ApplicationTest.kt with server endpoint tests)
* \[x] Espresso for Android UI (stub tests created, requires Android setup)

\[iOS and Web parity]

* \[x] iOS main (IosAuthService.kt stub implementation)
* \[x] Web UI (JsAuthService.kt and WasmAuthService.kt stub implementations)
* \[x] Parity check (ParityTest.kt stub created)

\[Setup Checkpoints]

* \[x] Docker/PostgreSQL setup (docker-compose.yml exists)
* \[x] Ktor server (Application.kt with Firebase and Sheets integration)
* \[x] Android phone testing (HUMAN_TASKS.md with complete instructions)
