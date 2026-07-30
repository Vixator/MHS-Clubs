# MHS Clubs — Implementation Summary

Last reviewed: 2026-07-24

## Verified repository state

- The Ktor server is the only component that uses the NocoDB token. It verifies Firebase ID tokens, enforces configured student/staff domains, and fails closed when Firebase Admin is unavailable.
- The server uses NocoDB REST and a Docker image ([Dockerfile.server](Dockerfile.server)); PostgreSQL, SQLDelight, and a database container are not part of this architecture.
- `.env` is loaded by the server. It is discovered by walking upward from the process working directory, so `:server:run` finds the repository-root `.env` even when Gradle starts in `server/`. Relative service-account paths resolve beside that `.env` file.
- `WEB_ALLOWED_HOST` is validated as a hostname (with an optional leading `*.`) and supplied to Ktor CORS; localhost is always permitted for HTTP/HTTPS development. The prior hard-coded school domain is removed.
- The Web app uses Firebase browser authentication and `JsClubContentApi` for authenticated club-directory, membership, event, announcement, RSVP, and attendance requests.
- `jsBrowserDevelopmentRun` serves the processed Web resources from `app/webApp/build/processedResources/js/main`; a local request to `http://localhost:8081/` returned HTTP 200 with `index.html` and `firebase-auth.js` on 2026-07-24.

## Reported cloud state (not independently inspectable from this repo)

The session handoff reports that Firebase Google sign-in, Android and Web Firebase registration, both service-account keys, NocoDB Cloud tables/token/table IDs, Calendar sharing, and local server/Docker health checks are complete. The repository confirms code/configuration support for these claims, but does not contain cloud credentials or a cloud-state export to prove them.

## NocoDB schema contract

`club_id` always stores the NocoDB `clubs.Id` primary key, never the club `code`.

| Table | Columns / exact values |
| --- | --- |
| `clubs` | Imported club fields; related tables reference built-in `Id`. |
| `users` | `Id`, `CreatedAt`, `display_name`, `firebase_uid`, `email`, `role` (`Student`, `Teacher`), `avatar_url`. |
| `memberships` | `firebase_uid`, `club_id`, `status` (`pending`, `active`, `revoked`), `is_club_admin`, `role` (`member`, `advisor`, `student_leader`). |
| `events` | `club_id`, `google_event_id`, `title`, `description`, `location`, `start_time`, `end_time`, built-in `UpdatedAt`. |
| `rsvps` | `event_id`, `firebase_uid`, `status` (`yes`, `no`, `maybe`). |
| `attendance` | `club_id`, `event_id`, `user_id`, `status` (`present`, `absent`, `late`), manual `recorded_at`. |
| `announcements` | `club_id`, `title`, `content`, `author_name`, `is_active`, built-in `CreatedAt`. Do not add `message_body` or `links`; those are relay payload fields only. |

## Known code gaps

- Preview-only `App()` instances retain sample data when no API adapter is supplied. Production Web and Android entry points provide authenticated adapters.
- Full end-to-end route and NocoDB-adapter tests still need an isolated NocoDB fixture; CI currently verifies server tests plus Web and Android compilation.

## Discrepancies corrected from earlier documentation

- Earlier docs incorrectly listed Firebase, NocoDB, service-account creation, and `.env` values as entirely pending. The handoff reports them configured; cloud-side completion remains unverified from local source.
- Earlier docs said all Compose content was sample data. Web and Android now use authenticated adapters for the club directory, memberships, events, announcements, RSVPs, and attendance.
- Calendar sync previously wrote `updated_at` and API adapters did not read NocoDB's built-in `CreatedAt`/`UpdatedAt`; the code now relies on those built-in fields and falls back to an event's start time when a timestamp is absent.
