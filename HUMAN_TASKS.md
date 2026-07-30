# MHS Clubs — Remaining Human, Credential, and Deployment Tasks

Last reviewed: 2026-07-24

This list intentionally excludes code work. Firebase, NocoDB, the local `.env`, both service-account keys, Android registration, the Web Firebase configuration, and Calendar sharing are reported complete in the session handoff. Their cloud-side state cannot be independently verified from this repository.

## Before production

- [ ] Choose and deploy a public HTTPS host for the Web app and a long-lived container host for the Ktor API; build the API image with `docker build -f Dockerfile.server -t mhs-clubs-server .`.
- [ ] Put all server variables from `.env` into the deployment secret manager, including the seven `NOCODB_*_TABLE` IDs and `FORM_INGEST_SECRET` when Forms are enabled. Mount both service-account JSON files as secret files; do not commit them or expose them to clients.
- [ ] Set `WEB_ALLOWED_HOST` to the deployed Web hostname. The server accepts a hostname or `*.example.org` (without a path); localhost remains allowed for development.
- [ ] Add the deployed Web domain to Firebase Authentication's authorized domains and add the corresponding OAuth redirect/origin configuration in Google Cloud.
- [ ] Request the deployed `/health` endpoint and verify Firebase, NocoDB, and Google Calendar each report configured. Then test student, teacher, and external Google accounts against the deployed app.
- [ ] Establish a yearly club-data import/review process.

## Platform work that requires the project owner

- [ ] For a physical Android-device local test, pass this computer's LAN API URL with `-PmhsClubsApiBaseUrl=http://192.168.x.x:8080`, allow TCP 8080 through Windows Firewall, and keep the phone and computer on the same Wi-Fi. (`10.0.2.2` is only for an emulator; release builds must use HTTPS.)

## Google Form announcement relay

- [x] The announcement Form, Apps Script submit trigger, and matching `FORM_INGEST_SECRET` are configured.
- [ ] Store the exact Form dropdown-label → NocoDB `clubs.Id` mapping in the Apps Script `MHS_CLUBS_CLUB_IDS` Script Property. Restrict Form access to teachers.
- [ ] Before production, point the relay at the deployed HTTPS API URL; Apps Script cannot call localhost or a LAN-only server.

## Not required

- No Google Calendar OAuth, Calendar token storage, Google Sheets API, PostgreSQL, SQLite, SQLDelight migration, Docker database, NocoDB backup process, or iOS support.
