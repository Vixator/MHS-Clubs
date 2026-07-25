# MHS Clubs — Product Requirements

Version 3.0 — 2026-07-22

## Purpose

MHS Clubs is a Kotlin Multiplatform application for students and staff to discover clubs, manage memberships, schedule club events, share announcements, RSVP, and record attendance. Android, Web, and iOS share Compose code; Ktor is the application API.

## Authentication and roles

The application uses Firebase Authentication with Google Sign-In in a personal Firebase project and linked personal Google Cloud project. The Google picker may show any Google account and must not use an organization-owned OAuth setup or a Google hosted-domain restriction.

After Firebase authenticates the account, Ktor verifies the Firebase ID token. Access is allowed only when the email is verified and ends in an allowed school suffix:

| Suffix | Role | Access |
| --- | --- | --- |
| `@students.mcpasd.k12.wi.us` | Student | Browse clubs, join clubs, see club events and announcements, RSVP. |
| `@mcpasd.k12.wi.us` | Teacher Administrator | Student access plus administration of all clubs. |
| Any other suffix | Rejected | No app access. |

The deployment settings `STUDENT_EMAIL_DOMAIN` and `STAFF_EMAIL_DOMAIN` control these values. Every teacher is automatically a global administrator. A teacher may set a student's membership `is_club_admin` flag; that student can manage memberships, events, announcements, and attendance for that club only. There are no Firebase custom claims.

## Club data and calendar

The pre-existing annual club spreadsheet is not queried by the app. A person cleans it and provides a UTF-8 CSV using [data/club-import-template.csv](data/club-import-template.csv); that file is imported into NocoDB. Empty values should remain empty rather than being guessed.

Students see club meetings and events in the application’s built-in calendar. The product does not request Google Calendar scopes from student/teacher clients and does not perform user Google Calendar OAuth. A server-side Google service account reads each club's shared Google Calendar and mirrors safe event data into NocoDB for app display.

## Data backend and data model

NocoDB is the source of truth, exposed only through its REST API to Ktor. Client applications never receive a NocoDB API token and never call NocoDB directly.

Required NocoDB tables are:

- `clubs`: NocoDB primary key (`Id`/`id`) as canonical club identifier, plus name, code, description, category, meeting day/time/location, advisor details, active status, and `Calendar` (real Google Calendar ID).
- `users`: Firebase UID, verified email, display name, role, avatar URL, timestamps.
- `memberships`: `firebase_uid`, `club_id`, status, and Boolean `is_club_admin` for teacher-assigned club administration.
- `events`: `club_id` (clubs `Id`/`id`), `google_event_id`, title, description, location, start/end times, and update timestamp.
- `rsvps`, `attendance`, and `announcements`.

No SQLite, PostgreSQL, SQLDelight, database container, SQL migration, Google Sheets API, or client-side Calendar OAuth integration is in scope.

## Core features

- Authenticated club discovery, search, and joining.
- Teacher administration across all clubs and teacher-assigned student administration for a specific club.
- Club events, in-app calendar, RSVPs, and attendance tracking.
- Club announcements.
- Clear sign-in rejection for unverified/non-school identities.

## Security and operations

- Verify all Firebase ID tokens on the server; fail protected requests closed when Firebase Admin is missing.
- Enforce role decisions on the server, never from client-supplied email or role values.
- Store Firebase service-account credentials and NocoDB tokens only in deployment secrets.
- Deploy Ktor from the versioned server container (`Dockerfile.server`) so runtime setup is consistent across environments.
- Use TLS between clients, Ktor, Firebase, and NocoDB.
- Maintain NocoDB backup/restore procedures and review annual CSV imports before publishing them.

## Acceptance criteria

The first release is ready when a verified student and a verified staff account can sign in through the personal Firebase project; external emails are denied; staff receives administrative access; club data is served from NocoDB; students can view club events in the in-app calendar without any Google Calendar permission prompt; and event data is synchronized server-side from each club's shared Google Calendar.
