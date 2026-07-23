# MHS Clubs — Human Setup Checklist

## Personal Firebase and Google Cloud

- [ ] Create a personal Firebase project and its linked personal Google Cloud project.
- [ ] Enable **Google** in Firebase Authentication. Do not configure an organization-only hosted-domain restriction.
- [ ] Add Android, Web, and iOS applications to Firebase.
- [ ] Set `firebase_web_client_id` in `app/shared/src/androidMain/res/values/firebase.xml` to the Firebase Web OAuth client ID, then configure the Android SHA-1/SHA-256 fingerprints in Firebase.
- [ ] Replace `app/webApp/src/webMain/resources/firebase-config.js` placeholders with the Firebase Web app configuration and authorize the deployed Web domain in Firebase Authentication.
- [ ] In Xcode, add the FirebaseAuth, FirebaseCore, and GoogleSignIn packages; add `GoogleService-Info.plist` to the iOS target and its reversed-client-ID URL scheme to `Info.plist`.
- [ ] Create a Firebase Admin service account key and place it only in your local/deployment secret manager. Set `FIREBASE_SERVICE_ACCOUNT` to its absolute runtime path and `FIREBASE_PROJECT_ID` to the project ID.
- [ ] Add the production web origin and OAuth redirect URIs to the personal Google Cloud OAuth client when the Web client is deployed.

## School-domain policy

- [ ] Set `STUDENT_EMAIL_DOMAIN=students.mcpasd.k12.wi.us` and `STAFF_EMAIL_DOMAIN=mcpasd.k12.wi.us` in the server/deployment environment.
- [ ] Test one student, one teacher, and one external Google account. Firebase may authenticate all three, but only the first two verified school identities may use the app; every teacher must receive global administration access.

## NocoDB

- [ ] Create or choose a NocoDB workspace/project and create a `clubs` table.
- [ ] Import the cleaned annual CSV using [data/club-import-template.csv](data/club-import-template.csv) as the column contract.
- [ ] Copy the `clubs` table ID from NocoDB and set `NOCODB_CLUBS_TABLE`.
- [ ] Create a dedicated server API token with only the required project/table permissions; set `NOCODB_API_TOKEN` and `NOCODB_BASE_URL` as server secrets.
- [ ] Create tables for users, memberships, events, RSVPs, attendance, and announcements, then set all seven `NOCODB_*_TABLE` values.
- [ ] Make the `memberships` table include `firebase_uid`, `club_id`, `status`, and Boolean `is_club_admin` columns. Teachers elevate/revoke a student through `PUT /api/memberships/{membershipId}/club-admin` with `{"enabled":true|false}`.
- [ ] Use `club_id` on events, announcements, and attendance records; club-admin routes enforce the club in their URL.
- [ ] Establish NocoDB backups and a yearly import/review process before replacing production club data.

## Ktor API server

- [ ] Deploy the `server` module to a host that can run a long-lived JVM/Kotlin service. This is the authorization layer between the apps and NocoDB; do not give the iOS, Android, or Web apps the NocoDB API token.
- [ ] For a local smoke test, set the required environment variables and run `./gradlew.bat :server:run`. Confirm `http://localhost:8080/health` returns `"firebaseInitialized":true` and `"nocoDbConfigured":true`.
- [ ] In the deployment secret manager, set `FIREBASE_PROJECT_ID`, `STUDENT_EMAIL_DOMAIN`, `STAFF_EMAIL_DOMAIN`, `NOCODB_BASE_URL`, `NOCODB_API_TOKEN`, and every `NOCODB_*_TABLE` value. Never commit or expose these values to a client application.
- [ ] Store the Firebase Admin service-account JSON as a deployment secret file, mount it at runtime, and set `FIREBASE_SERVICE_ACCOUNT` to its mounted absolute path. Do not put the JSON contents in source control or a client-accessible environment variable.
- [ ] Let the hosting platform provide `PORT`; Ktor listens on `PORT` and uses `8080` only when it is absent for local development.
- [ ] Set `WEB_ALLOWED_HOST` to the production Web app's hostname only (for example, `clubs.example.org`), without `https://` or a path. This permits browser requests from that one deployed site; add its domain to Firebase Authentication's authorized domains as well.
- [ ] Configure HTTPS and a stable API hostname, then make that hostname available to the Android, iOS, and Web clients when their API integration is enabled.
- [ ] After deployment, request `https://<api-host>/health`, verify it reports both services as configured, then sign in with a student, teacher, and external account to confirm the domain and role policy.

## Explicitly not required

- No Google Calendar API, Calendar OAuth scopes, calendar token storage, or Google Calendar consent screen review.
- No Google Sheets API/service account.
- No SQLite, PostgreSQL, SQLDelight migration, Docker database, or database credentials.
