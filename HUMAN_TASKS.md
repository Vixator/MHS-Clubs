# MHS Clubs — Human Setup and Deployment Tasks

Last reviewed: 2026-07-16

This checklist contains only tasks that require human credentials, approvals, or
external account control.

## Discrepancies Found

- Previous file mixed human-only work with engineering implementation tasks (CRUD routes, OAuth endpoint coding, sync orchestration, tests). Those coding items were removed from this file and are tracked in `PLAN.md`.
- Previous file included build-restoration work as a "human task"; that is engineering work, not inherently credential/approval-bound.
- Repository checks show `GOOGLE_SHEETS_CREDENTIALS` and `GOOGLE_CALENDAR_CREDENTIALS` file paths in `.env` do not currently exist locally, so credential provisioning remains a real human-owned prerequisite.

## 1. Firebase and Google Cloud project ownership

- [ ] Create/select the Firebase project used for MHS Clubs production/staging.
- [ ] Enable Google Sign-In in Firebase Authentication.
- [ ] Create OAuth clients (Android + Web/Server) in the same Google Cloud project.
- [ ] Enable Google Calendar API and Google Sheets API.
- [ ] Update OAuth consent screen scopes to include `https://www.googleapis.com/auth/calendar.events`.
- [ ] Complete any Google verification required for expanded calendar scopes.
- [ ] Restrict authorized domains, redirect URIs, and origins for production.

## 2. Service accounts and secrets management

- [ ] Provision and securely store Firebase Admin service-account credentials for server runtime.
- [ ] Provision and securely store Google Sheets service-account credentials with least privilege to required sheet(s).
- [ ] Decide whether shared-calendar service-account integration is needed in production; if yes, provision credentials and a managed calendar ID.
- [ ] Create and store `GOOGLE_OAUTH_CLIENT_ID` and `GOOGLE_OAUTH_CLIENT_SECRET` in deployment secrets.
- [ ] Generate and store `CALENDAR_TOKEN_ENCRYPTION_KEY` (base64-encoded random 32-byte key) in deployment secrets.
- [ ] Define token-key rotation and re-consent operations before production rollout.

## 3. Environment and deployment controls

- [ ] Populate production/staging secret manager entries for all required runtime variables.
- [ ] Confirm production database provisioning, backup policy, and credential rotation policy.
- [ ] Configure explicit production CORS origins and validate browser behavior on real domains.
- [ ] Configure production monitoring/alerting and log retention with OAuth secret redaction requirements.

## 4. Governance and release approvals

- [ ] Security/privacy review approval for student data handling, OAuth token storage, and calendar synchronization behavior.
- [ ] School/organization approval for OAuth consent language and data-retention policy.
- [ ] Final release sign-off after staged validation on real cloud infrastructure.
