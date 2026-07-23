# MHS Clubs — Delivery Plan

Status: `[x]` complete, `[~]` partial, `[ ]` pending.

## New technical direction

- [x] Personal Firebase/Google Cloud project is the authentication owner.
- [x] Firebase Google sign-in requests only identity: ID token, email, and profile.
- [x] Server rejects unverified or non-school email suffixes; it treats `@students.mcpasd.k12.wi.us` as Student and `@mcpasd.k12.wi.us` as Teacher Administrator. Both suffixes are environment-configurable.
- [x] Teachers automatically administer all clubs and can grant/revoke `is_club_admin` for a student's membership; club admins are limited to their own club.
- [x] Firebase Admin verification fails closed when its service account is absent.
- [x] Google Sheets, Google Calendar sync, Calendar OAuth, Calendar token storage, and their tests/services are removed.
- [x] SQLDelight schema/plugin and local PostgreSQL/Docker configuration are removed.
- [x] NocoDB REST client is server-only and supports configured clubs, users, memberships, events, RSVPs, attendance, and announcements tables.
- [x] Built-in calendar no longer offers Google Calendar sync.

## Remaining application work

- [x] Authenticated Ktor REST routes cover all configured NocoDB resources; staff-only writes are enforced server-side.
- [x] Android, Web, and iOS Firebase auth bridges are in place. Android login is wired to the native activity result; Web uses Firebase popup sign-in; iOS bridges the native Swift sign-in result into Compose.
- [ ] Replace client sample data with authenticated Ktor API calls during frontend integration.
- [ ] Complete Web and iOS Firebase sign-in.
- [ ] Add route, NocoDB-adapter, and client integration tests; add CI.

## Data import contract

The annual source is a human-cleaned UTF-8 CSV, imported into NocoDB. Use [data/club-import-template.csv](data/club-import-template.csv). Required columns are `name` and `code`; retain the other template columns where known. Use empty cells for unknown values, not invented data.
