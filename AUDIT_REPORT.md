# MHS Clubs — Architecture Decision Record

Last updated: 2026-07-22

## Decision

The product uses a personal Firebase/Google Cloud project for Google identity, verifies Firebase ID tokens in Ktor, and permits only verified MCPASD student or teacher email suffixes. Teachers are administrators for every club and can elevate students through club membership records. Google account selection is intentionally unrestricted at the provider level; authorization happens after the verified email is received.

NocoDB is the server-side data backend. Ktor is the only component allowed to hold the NocoDB API token. The annual club spreadsheet is cleaned and imported manually as CSV; there is no live Google Sheets integration.

The calendar is entirely in-app. The product does not request, store, read, or write Google Calendar permissions or data. SQL databases and SQLDelight are not part of the architecture.

## Security controls

- Firebase Admin absence fails protected requests closed.
- Email verification and allowed suffix checks are server-side.
- Email suffixes are configurable deployment settings, not client-provided data.
- NocoDB tokens and Firebase service-account files are server/deployment secrets and must never be committed or shipped to clients.

## Follow-up risks

The remaining CRUD endpoints still need NocoDB mappings, authorization tests, pagination, validation, backups, and rate limiting before production release. Firebase email suffixes are an authorization rule, not proof of a person's school affiliation beyond control of that account; confirm the district's actual suffixes before deployment.
