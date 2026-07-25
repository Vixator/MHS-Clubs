# MHS Clubs — Remaining Human, Credential, and Deployment Tasks

Last reviewed: 2026-07-24

This list intentionally excludes code work. Firebase, NocoDB, the local `.env`, both service-account keys, Android registration, the Web Firebase configuration, and Calendar sharing are reported complete in the session handoff. Their cloud-side state cannot be independently verified from this repository.

## Before production

- [ ] Choose and deploy a public HTTPS host for the Web app and a long-lived container host for the Ktor API; build the API image with `docker build -f Dockerfile.server -t mhs-clubs-server .`.
- [ ] Put all server variables from `.env` into the deployment secret manager, including the seven `NOCODB_*_TABLE` IDs and `FORM_INGEST_SECRET` when Forms are enabled. Mount both service-account JSON files as secret files; do not commit them or expose them to clients.
- [ ] Set `WEB_ALLOWED_HOST` to the deployed Web hostname. The server accepts a hostname or `*.example.org` (without a path); localhost remains allowed for development.
- [ ] Add the deployed Web domain to Firebase Authentication's authorized domains and add the corresponding OAuth redirect/origin configuration in Google Cloud.
- [ ] Request the deployed `/health` endpoint and verify Firebase, NocoDB, and Google Calendar each report configured. Then test student, teacher, and external Google accounts against the deployed app.
- [ ] Establish NocoDB backups and a yearly club-data import/review process.

## Platform work that requires the project owner

- [ ] Complete iOS Firebase native setup on macOS: add FirebaseAuth, FirebaseCore, and GoogleSignIn; add `GoogleService-Info.plist`; configure the reversed-client-ID URL scheme; then build/test the iOS target.
- [ ] For a physical Android-device local test, select this computer's LAN API URL, allow TCP 8080 through Windows Firewall, and keep the phone and computer on the same Wi-Fi. (`10.0.2.2` is only for an emulator.)

## Optional Google Form announcement relay

- [ ] If announcement forms are wanted, create the form, put [google-form-announcement.gs](scripts/google-form-announcement.gs) in its Apps Script project, configure its Script Properties, and install the submit trigger.
- [ ] Give the relay a reachable HTTPS API URL; Apps Script cannot call localhost or a LAN-only server.

## Not required

- No Google Calendar OAuth, Calendar token storage, Google Sheets API, PostgreSQL, SQLite, SQLDelight migration, or Docker database.
