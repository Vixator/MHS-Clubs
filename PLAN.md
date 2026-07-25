# MHS Clubs — Delivery Plan

Status: `[x]` verified in the repository, `[~]` implemented but needs cloud/device validation, `[ ]` pending engineering work.

## Architecture

- [x] Firebase Authentication supplies Google identity; Ktor verifies Firebase ID tokens and accepts only configured student/staff school domains.
- [x] NocoDB is the server-side data backend. Clients never receive the NocoDB token or call NocoDB directly.
- [x] The canonical club ID is NocoDB `clubs.Id`; all `club_id` fields use that ID.
- [x] Google Calendar is read by a server service account and mirrored into NocoDB events. No student/teacher Calendar OAuth or token storage exists.
- [x] The server is containerized by `Dockerfile.server`; there is no PostgreSQL/SQL/SQLite/SQLDelight runtime component.
- [x] The server auto-loads repository-root `.env` for `:server:run`, resolves relative service-account paths from it, and has configurable CORS via `WEB_ALLOWED_HOST`.
- [x] Kotlin/JS dev server serves processed Web resources on port 8081; `/` was verified as HTTP 200 locally.

## Application integration

- [x] Android and Web event/announcement adapters call authenticated `/api/my/events` and `/api/my/announcements`.
- [ ] Add a server-backed club directory contract: list/search clubs, retrieve a detail, and join by code or ID while preserving authorization boundaries.
- [ ] Extend `ClubContentApi` and `App.kt` state/UI to use that contract; remove sample club/membership data from browse/detail/join flows.
- [ ] Replace remaining sample event-detail, attendance, and other content paths with authenticated APIs.
- [~] Build and test native iOS Firebase configuration on macOS (owner action required; see [HUMAN_TASKS.md](HUMAN_TASKS.md)).
- [ ] Add route, NocoDB-adapter, and client integration tests plus CI.

## Deployment and operations

- [~] Cloud credentials/NocoDB schema and local health checks are reported complete in the session handoff but cannot be independently verified from source control.
- [ ] Complete public Web/API deployment, secret mounting, Firebase authorized-domain configuration, and role-policy smoke tests. These owner tasks are listed in [HUMAN_TASKS.md](HUMAN_TASKS.md).
- [ ] Establish NocoDB backups and annual data review/import.

## Schema reference

The exact NocoDB table contract is maintained in [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md#nocodb-schema-contract). Exact select values are significant: membership `pending|active|revoked`; membership role `member|advisor|student_leader`; RSVP `yes|no|maybe`; attendance `present|absent|late`; user role `Student|Teacher`.
