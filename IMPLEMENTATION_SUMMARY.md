# MHS Clubs — Implementation Summary

Last updated: 2026-07-22

## Implemented in this migration

- The Android Firebase Google sign-in request no longer asks for server authorization codes or `calendar.events` permission.
- Server authentication verifies Firebase ID tokens and enforces verified MCPASD student/teacher email suffixes. Every teacher-domain user is a global administrator; students are not. A missing Firebase Admin setup rejects protected requests instead of accepting arbitrary tokens.
- Domains default to `students.mcpasd.k12.wi.us` and `mcpasd.k12.wi.us`, with `STUDENT_EMAIL_DOMAIN` and `STAFF_EMAIL_DOMAIN` overrides.
- Teachers can elevate or revoke a student's club-specific admin access through the membership `is_club_admin` field.
- Google Sheets import, Google Calendar services, per-student Calendar token code, Calendar sync routes, related tests, SQLDelight files, and PostgreSQL Docker configuration are removed.
- `NocoDbClient` is a server-only NocoDB v2 Records API adapter. Authenticated Ktor routes cover each configured NocoDB resource; staff-domain users are required for writes.
- The Compose calendar remains an internal club-event calendar and has no Google sync control or sync-status presentation.

## Important current boundary

NocoDB resource routes are ready once the matching `NOCODB_*_TABLE` values are supplied. The existing Compose screens still use sample data; frontend API state integration is intentionally the next phase.

## Required configuration

See [HUMAN_TASKS.md](HUMAN_TASKS.md). The next input needed is a cleaned UTF-8 club CSV using [data/club-import-template.csv](data/club-import-template.csv), plus your personal Firebase credentials and NocoDB table/token values.
