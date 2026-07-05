# MHS Clubs Application
## Product Requirements Document (Combined)
**Version:** 2.0 (Merged from v1.0 and Android-focused draft)

> **Note:** Earlier drafts referenced Flutter and NocoDB; both have been dropped in favor of a unified Kotlin stack. See Section 11 for the finalized technical stack.

---

## 1. Executive Summary

MHS Clubs is a mobile-and-web application designed to centralize extracurricular information for high school (and potentially college) students. It enables users to discover, join, and manage clubs, organize and RSVP to events, communicate with fellow members, and track club activity — all from a single platform. The app integrates authentication, real-time updates, and calendar sync to eliminate fragmented communication across clubs and provide one unified hub for student involvement.

---

## 2. Product Vision

Empower students, club officers, and educators to streamline club operations, foster collaboration, and increase student engagement through a centralized, easy-to-use platform.

---

## 3. Goals

**Primary Goal:**
Provide a seamless experience for browsing/joining clubs, managing events, and communicating with members.

**Secondary Goals:**
- Enable real-time notifications for events and club updates.
- Support multi-platform access (Android, iOS, and Web).
- Ensure the system scales as more clubs and schools are added.
- Eliminate fragmented, inconsistent communication across individual clubs.

---

## 4. Product Overview

The application allows students to authenticate (ideally via school-issued Google accounts), browse available clubs, join organizations, view personalized announcements, and see upcoming meetings synced to their calendar. Club administrators and officers can manage membership, post updates, and organize events. Data is managed through backend services handling authentication, club/event data storage, and calendar integration.

---

## 5. Key Features & Functional Requirements

Users must authenticate before accessing app features. All club, event, and membership data is filtered and displayed based on the authenticated user's role and memberships.

### 5.1 User Authentication & Roles
- Sign-in restricted to school Google accounts only, distinguished by email domain:
  - `@students.mcpasd.k12.wi.us` → **Student** role
  - `@mcpasd.k12.wi.us` → **Teacher** role
  - All other Google accounts are rejected at sign-in.
- Role model (no Guest role — authentication is required for any app access):
  - **Student:** base/member access — browse clubs, join clubs, view announcements, view meeting schedules, RSVP.
  - **Student Leader:** a Student who has been granted admin privileges for one or more specific clubs (see below) — admin capabilities scoped only to their assigned club(s).
  - **Teacher:** automatically granted admin access to **all** clubs — no manual assignment needed, determined directly by email domain at sign-in.
- Teachers can grant Student Leader (club-scoped admin) access to specific students for the club(s) that student leads.

### 5.2 Club Management
- Club information is sourced from a Google Sheet via the Google Sheets API (read-only — the app never writes back to the Sheet).
- Teachers (Admin for all clubs) and Student Leaders (Admin for their specific club only) can edit club details within the app (e.g., meeting time/location changes); these edits are stored as local overrides in PostgreSQL and do not modify the source Google Sheet.
- Students join clubs via search or invite link.
- Teachers can manage member lists and grant/revoke Student Leader status for any club; Student Leaders can manage member lists only for their own club.

### 5.3 Event & Meeting Management
- Schedule meetings/events with date, time, location, and description.
- RSVP system for members to confirm attendance intent.
- **Attendance tracking:** Teachers/Student Leaders can record actual attendance for each club meeting date, distinct from RSVP (who said they'd come vs. who actually showed up).
- **Meeting notes (optional/future):** ability to attach brief notes to a specific meeting (e.g., topics covered, action items) for club members to reference.
- Automatic sync of meeting dates to each student's Google Calendar (if calendar permission is granted).
- A built-in, in-app calendar view showing meeting dates for all clubs a student belongs to, functioning independently of Google Calendar sync — ensures core scheduling visibility even if a student declines calendar permission.

### 5.4 Announcements
- Teachers and Student Leaders can post announcements to their specific club's group.
- Announcements are visible to all members of that club within the app.
- No in-app messaging or open discussion forums are included in scope.

### 5.5 Search & Discovery
- Students can search and browse the full club directory, filtered by interest/category.

### 5.6 Analytics & Reporting *(Teacher/Student Leader-facing)*
- Track club activity: member growth and recorded attendance over time (see Section 5.3).
- Export attendance/membership reports (e.g., CSV) for record-keeping.

---

## 6. User Stories

**As a Student:**
- I want to browse and join multiple clubs so I can get involved in activities I'm interested in.
- I want event/meeting reminders synced to my calendar so I don't miss them.
- I want to see announcements from clubs I'm a member of.

**As a Student Leader:**
- I want to manage my club's member list and post announcements to my club's members.
- I want to schedule events, track RSVPs, and record attendance for my club's meetings.

**As a Teacher:**
- I want automatic admin access across all clubs without manual setup.
- I want to grant Student Leader status to specific students for the clubs they lead.
- I want to generate attendance reports for record-keeping.

---

## 7. System Architecture

The application follows a client-facing architecture where a shared codebase serves Android, iOS, and Web clients. External services handle authentication, calendar sync, and — depending on the final backend decision — data storage. Local/client-side storage may be used for caching membership state to reduce redundant network calls, with the backend as the source of truth.

---

## 8. Data Model

Core entities:
- **User** — student or teacher profile, authentication identity, role (Student, Student Leader, or Teacher), determined at sign-in by school email domain
- **Club** — name, description, logo, category, officer/admin list — base data sourced (read-only) from Google Sheets via the Sheets API; mirrored into PostgreSQL for performance
- **Club Override** — local, in-app edits to club details (meeting time/location changes) made by Teachers or Student Leaders — stored in PostgreSQL, layered on top of the base Sheet data, and never written back to the source Sheet
- **Membership** — links Users to Clubs, tracks role/status (including Student Leader designation, scoped to specific clubs) — stored in PostgreSQL
- **Event / Meeting** — date, time, location, description, associated club — stored in PostgreSQL, displayed in the in-app calendar, and optionally synced to each student's personal Google Calendar via the Calendar API
- **Attendance** — links Users to a specific Event/Meeting, tracks actual recorded attendance (distinct from RSVP) — stored in PostgreSQL
- **RSVP** — links Users to Events, tracks attendance intent/status — stored in PostgreSQL
- **Announcement** — club-specific update, visible to all members of that club — stored in PostgreSQL
- **Meeting Note** *(optional/future)* — brief notes attached to a specific meeting — stored in PostgreSQL

---

## 9. User Experience

Core screens: Authentication, Club Directory, Club Details, Calendar/Events, Announcements, and (for admins) a Management/Admin dashboard. Navigation should minimize the number of steps between browsing a club and joining it, and between viewing an event and RSVPing or adding it to a personal calendar.

---

## 10. Non-Functional Requirements

- **Performance:** App should remain responsive even with a growing number of clubs/events.
- **Reliability:** External service failures (auth provider, calendar API, backend) should degrade gracefully, not crash the app.
- **Security:** Role-based access control throughout, scoped correctly for Student, Student Leader, and Teacher roles; student information is encrypted at rest (database-level encryption) and in transit (TLS for all client-server API traffic).
- **Maintainability:** Modular architecture to isolate third-party service dependencies.
- **Scalability:** Architecture should support growth to additional clubs and, potentially, additional schools.

---

## 11. Technical Stack

- **Client:** Kotlin Multiplatform with Compose Multiplatform (shared UI across Android, iOS, and Web)
- **Server:** Ktor (Kotlin backend)
- **Database:** PostgreSQL, accessed via SQLDelight
  - **Development environment:** PostgreSQL hosted locally on the developer's machine
  - **Production environment:** to be determined (self-hosted vs. managed hosting) as the project matures
- **Authentication:** Firebase Authentication (Google Sign-In), restricted to `@students.mcpasd.k12.wi.us` and `@mcpasd.k12.wi.us` domains, with incremental OAuth scope for Google Calendar access
- **Club Data Source:** Google Sheets API
- **Calendar Integration:** Google Calendar API, syncing meeting/event dates to each individual student's own Google Calendar via their personal Google OAuth credentials (obtained through Firebase-managed sign-in)
- **Data Encryption:** TLS in transit; database-level encryption at rest for student information

---

## 12. Risks & Mitigation

Key risks include dependency on third-party services (authentication provider, database/backend service, Google Calendar API). These are mitigated through modular architecture and graceful error handling so that a single service outage doesn't take down the whole app.

---

## 13. Milestones

| Phase | Deliverables | Timeline |
|---|---|---|
| Phase 1 | MVP with user authentication and club creation/browsing | 2 weeks |
| Phase 2 | Event management and in-app communication tools | 3 weeks |
| Phase 3 | Analytics/reporting and full cross-platform (iOS/Web) parity | 2 weeks |

*Timelines are estimates from the source draft and should be revisited once the final tech stack and scope are confirmed.*

---

## 14. Stakeholders

- **Developers:** Android/iOS/Web engineers, backend developer(s).
- **Testers:** QA for user acceptance and regression testing.
- **Users:** Students, club admins/officers, faculty sponsors.

---

## 15. Future Enhancements

- Push notifications
- Officer/admin dashboards (expanded)
- RSVP system (if not included in MVP)
- Attendance tracking (if not included in MVP)
- Multi-school support

---

## 16. Acceptance Criteria

The application is considered successful when users can reliably authenticate, browse the club directory, join organizations, RSVP to events, and view personalized club content and announcements.

---

## 17. Development Environment & Operational Considerations

### 17.1 Local Development Database
- PostgreSQL is run locally via **Docker Desktop** (already installed) rather than a native install, using a `docker-compose.yml` to define the database service, credentials, and a persistent volume.
- This allows the dev database to be reset, version-pinned, and reproduced easily without system-wide installation.
- Production hosting (self-hosted vs. managed) is to be determined as the project matures.

### 17.2 Client-Server Architecture (Data Access)
- Client apps (Android, iOS, Web) never connect directly to PostgreSQL. All data access goes through the Ktor server's HTTP API.
- This keeps database credentials off client devices and ensures server-side authorization logic (e.g., admin-only actions, per-user RSVP restrictions) cannot be bypassed.
- Data flow: **Client app → Ktor server (HTTP API) → PostgreSQL (in Docker)**.

### 17.3 Development Machine & Test Devices
- All local development — app code, Ktor server, and the Dockerized PostgreSQL instance — runs on the same desktop machine.
- An Android phone is available for physical device testing, connected to the same local network as the development machine.
- The Ktor server is bound to `0.0.0.0` (not just `localhost`) so it is reachable from the test phone via the desktop machine's LAN IP address.

### 17.4 Connection & Credentials
- Database connection strings and other secrets are stored in a `.env` file, excluded from version control via `.gitignore`.
- A dedicated Postgres role/user is used for the application, rather than the default superuser account, to limit access scope.

### 17.5 Schema Migrations
- SQLDelight-generated code requires a defined migration strategy (versioned `.sqm` migration files) to evolve the schema over time without data loss, established early rather than retrofitted later.

### 17.6 Local Network Access for Device Testing
- Testing the mobile app on the physical Android device against the local server requires the device to be on the same Wi-Fi network as the development desktop, and the app configured to point at the desktop's LAN IP rather than `localhost`.
- The Android emulator, if used instead of/alongside the physical device, requires separate handling (it reaches the host machine's `localhost` via `10.0.2.2`).

### 17.7 Backups
- Regular backups (e.g., via `pg_dump`, or Docker volume snapshots) are recommended even during development once meaningful test data accumulates, to avoid data loss during schema changes or experimentation.

### 17.8 Firebase Service Account Security
- The Firebase service account JSON key (used for server-side Google Calendar API access) is treated as a secret: excluded from version control and not committed to the repository.

### 17.9 CORS Configuration
- The Ktor server requires CORS configuration to accept requests from the Compose Web client's origin, to avoid silent request failures once the Web target is active.

### 17.10 Guided Setup Checkpoints (Instructions for AI-Assisted Development)
When implementation reaches the following points, the assistant (Claude Code) should pause and walk the developer through the setup step-by-step, rather than assuming prior configuration is complete:
- **Docker/PostgreSQL setup:** Guide through installing/verifying Docker Desktop is running, creating the `docker-compose.yml`, starting the container, and confirming the database is reachable.
- **Ktor server setup:** Guide through configuring the server to connect to the Dockerized PostgreSQL instance, binding to `0.0.0.0`, and confirming the server runs and responds locally.
- **Android phone testing:** Guide through finding the desktop machine's LAN IP, configuring the Android app to point to it instead of `localhost`, connecting the phone to the same network, building/installing the app on the physical device, and verifying it can successfully reach the Ktor server.

These should be treated as hands-on checkpoints — confirming each step works before moving to the next — rather than a single block of instructions delivered all at once.

### 17.11 Google Sheets Integration
- **Source spreadsheet:** the master club list (activity, advisor, advisor email, meeting day/time/location, club code) lives in a Google Sheet, updated approximately once per year.
- **Read-only:** the app reads and extracts all rows from the Sheet via the Google Sheets API; it never writes back to the Sheet.
- **Local overrides:** admins, teachers, and student leaders can edit club details within the app (e.g., a meeting time change mid-year); these edits are stored in PostgreSQL as overrides layered on top of the Sheet-sourced base data, and do not affect the master spreadsheet.
- **Sync frequency:** given the Sheet updates roughly once a year, the server can perform a periodic (e.g., on-demand or scheduled) import/sync into PostgreSQL rather than querying the Sheets API on every app request — reduces API usage well within quota limits.
- **Access:** reading the Sheet requires a Google service account with the Sheet shared to it (read access only), or equivalent read-scoped OAuth credentials.

### 17.12 Google Calendar OAuth Considerations
- During sign-in, the app should request all necessary permissions upfront — including the Google Calendar scope alongside basic identity — via Firebase's incremental/additional-scope authorization, so that all features (including calendar sync) work without requiring a second permission prompt later.
- The server must securely store and refresh each student's OAuth access/refresh tokens in order to create or update calendar events on their behalf over time.
- If a student declines calendar permission, the app remains fully functional for club browsing, joining, and scheduling via the built-in in-app calendar view (see Section 5.3) — Google Calendar sync is treated as an enhancement, not a requirement, for core scheduling features.

### 17.13 Platform Priority
- **Android is the primary target** for initial development and testing.
- **iOS is not a current priority** but is planned — budget is available to cover iOS-specific costs (e.g., Apple Developer Program enrollment) when development reaches that stage.
- **Web** remains in scope alongside Android per the shared Compose Multiplatform codebase.

### 17.14 Deployment Costs
- Google Play Developer account (one-time fee) and Apple Developer Program (annual fee, when iOS work begins) will be covered once the app is ready for deployment.
- Any production hosting costs (if managed hosting is chosen over self-hosting — see Section 11) will also be covered at that stage.

### 17.15 Domain-Based Role Assignment (Implementation Note)
- Role assignment logic should validate the authenticated user's email domain at sign-in: `@students.mcpasd.k12.wi.us` → Student, `@mcpasd.k12.wi.us` → Teacher (Admin, all clubs). Any other domain should be rejected at sign-in.
- Student Leader status is a separate, per-club flag on the Membership record, grantable only by a Teacher (or, transitively, enforced server-side regardless of client input to prevent privilege escalation).

---

## 18. Conclusion

This document describes MHS Clubs as a unified platform connecting students, student leaders, and teachers to extracurricular activities — covering domain-restricted authentication, role-based club management, event scheduling, attendance tracking, club-scoped announcements, and calendar sync — with Android as the initial priority, and iOS/Web following, all from a shared Kotlin Multiplatform codebase.
