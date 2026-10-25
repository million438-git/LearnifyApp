
# Learnify Android: Real-World Build and Release Guide

**Reviewed:** 21 October 2026  
**Repository:** `TestApp`  
**Purpose:** A practical, codebase-specific path from the current Android prototype to a supportable public release.

## Executive assessment

Learnify already has a substantial native Android client: Java activities and XML layouts, local SQLite data, Python-backed learning tools, offline content, and student/admin screens. There is also a separate Spring Boot service. This is a meaningful feature prototype, but the inspected code does **not yet demonstrate a complete production system** for real users.

The main release blockers are architectural and operational, not visual:

1. The AI client uses `http://localhost:8080/`. On a user's phone, `localhost` means that phone, not the hosted backend. The app's network-security configuration permits cleartext only for local development addresses, so a public HTTPS endpoint must be designed and configured.
2. The Android build can inject `OPENAI_API_KEY` into `BuildConfig`, and `GpaCalculatorActivity` sends that key from the app. Any credential shipped in an APK can be extracted. Provider secrets must stay on a server.
3. `DatabaseHelper` seeds an admin named `admin` with the known password `admin123`. Do not distribute a build containing this account or use it as production provisioning.
4. Student, admin, payment, progress, and other records are currently represented in the device's SQLite database. The Spring service currently has an AI assistant route, not a complete authenticated, shared application API and database.
5. `PaymentActivity` and `AdminPaymentVerificationActivity` are currently non-exported in the manifest. Keep checking the merged release manifest, and do not treat non-exported screens or client-side role checks as server authorization.
6. `targetSdk` is 34, no release signing configuration is present in `app/build.gradle`, and no GitHub Actions workflow was found. Confirm current Play Console target-API rules before submission, and create repeatable release automation.
7. An Android build/test could not be verified: the Android SDK configured in `local.properties` is missing, and the Chaquopy Python path in `app/build.gradle` is machine-specific. Historical test/build claims in the UI audit are not a substitute for a fresh build from the current source.

**Recommended next move:** agree on a small first-release feature set and make one authenticated HTTPS backend the authority for accounts and shared user data. Do not start a public release by polishing more screens.

## Repository map: what each area is for

| Path | Role | What to do with it |
|---|---|---|
| `settings.gradle` | Android Gradle project and `:app` module registration | Keep the Android module definition here. |
| `build.gradle` | Android Gradle Plugin and Chaquopy plugin versions | Keep versions deliberate and upgrade with CI verification. |
| `gradle.properties` | Gradle/AndroidX build-wide flags | Keep only shared build settings; do not place secrets here. |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` | Pinned Gradle wrapper | Use the wrapper, not a developer's globally installed Gradle. |
| `app/build.gradle` | Android app ID, SDK levels, dependencies, Python packaging, release shrinker | Review before every SDK/dependency/release change. Remove embedded provider-key wiring and the developer-specific Python executable path before public or team builds. Add a controlled release signing setup outside source control. |
| `app/src/main/AndroidManifest.xml` | Permissions, launcher, app components and exported flags | Audit permissions and every `exported=true` component. Keep only the intended launcher/deep-link components externally accessible. |
| `app/src/main/java/com/million/learnify/` | 109 Java source files, currently in one flat package | This is the Android app. Activities make up the student, admin, learning, assessment, settings, camera/lab, and AI flows; shared classes handle database, authentication, themes, notifications, and AI. Keep the detailed screen/layout inventory in `LEARNIFY_UI_UX_AUDIT.md` in sync with actual code. |
| `app/src/main/res/layout/` | 108 XML screen, dialog, and list-item layouts | Maintain with the owning screen; test small screens, large text, dark theme, and localization. |
| `app/src/main/res/drawable/`, `mipmap-*/`, `anim/`, `menu/`, `values*/`, `color/`, `xml/` | Icons, images, animation, navigation, themes, locale strings, and platform configuration | Audit branding, contrast, translations, and resource references. The `xml/` network security policy currently allows cleartext only for local development; public API traffic should use HTTPS. |
| `app/src/main/assets/subjects/` | 84 bundled subject-learning content files | Confirm educational accuracy, licensing, locale, accessibility, and offline update strategy. |
| `app/src/main/assets/InteractiveLab/` | 117 bundled interactive lab files | Test WebView/content safety, accessibility, and device performance. Treat bundled content as release input, not as a backend. |
| `app/src/main/assets/daily_questions.csv` | Seed data for daily questions | Validate format, answer integrity, attribution, and content updates. |
| `app/src/main/python/` | `camera_lab_solver.py`, `voice_assistant_engine.py`, `voice_brain.py` called through Chaquopy | Verify packaging and behavior for every supported ABI/device; measure app size, cold start, permissions, and memory. The build also installs NumPy, OpenCV, SymPy, and rank-BM25. |
| `app/src/test/` | Android/JVM unit tests | Expand to cover auth and data migration, API behavior, validation, and business-critical calculations. |
| `app/src/androidTest/` | Android device/instrumentation tests | Add real-device/emulator tests for sign-in, core learning, offline behavior, and screen navigation. |
| `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` | Separate Maven/Spring Boot backend build | Run its tests independently from Gradle. It currently uses Java 25 in the POM; standardize and document the Java version for local development and CI. |
| `src/main/java/com/million/learnify/studentbackend/` | Spring Boot application, assistant controller, DTOs, Gemini service, and response normalization | This is the intended backend namespace configured as the Maven main class. Extend it for authenticated product APIs, persistence, authorization, validation, and operations. |
| `src/main/java/com/myapp/studentbackend/` and root-package backend classes | Duplicate/legacy backend namespace and class set | Decide which implementation is canonical; remove or migrate stale duplicates only after confirming no build or deployment depends on them. |
| `src/main/resources/application.properties` | Backend port/model/key configuration | Supply credentials through the hosting secret manager/environment; do not commit secrets. Set production-safe server configuration separately from local defaults. |
| `src/test/` | Backend unit/controller tests | Extend with auth, persistence, rate-limit, authorization, and failure-path tests. |
| `.github/modernize/` | Modernization/assessment reports and tooling assets | Treat as analysis material, not as an active build/deploy pipeline. No `.github/workflows/` directory was present in this review. |
| `LEARNIFY_UI_UX_AUDIT.md` | Existing detailed screen/UI audit and historical implementation notes | Useful design and feature inventory, but time-sensitive. For example, it reports several missing manifest activities that are registered in the manifest now. Re-check all claims against current source before acting on them. |
| Root screenshots, XML dumps, `application.backup`, `app/build.gradle.before-python-fix.bak`, `fix_folders.gradle` | Historical snapshots, debugging artifacts, or backup scripts | Not normal application source or release inputs. Decide which are still needed; keep them out of release packages and clean them up only after confirming ownership and value. |
| `.gradle/`, `build/`, `target/`, `local.properties` | Local/generated build state or machine-specific configuration | Never use as release source of truth. Keep local SDK paths and generated outputs out of source control. |

## Release plan: do these in order

Each stage has a completion gate. Do not move to public distribution just because the APK installs.

### 0. Establish a clean, reproducible baseline

1. Confirm the repository's intended source of truth and preserve any existing local changes.
2. Install a supported JDK, Android SDK/platform tools, Android SDK Platform 36 (as required by the current Android build), and the Python version/tooling required by Chaquopy. Ensure the build does not depend on the current author's absolute `buildPython` path.
3. From the repository root, run:

   ```powershell
   .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
   .\mvnw.cmd test
   ```

4. Record the exact JDK, Gradle, Android SDK, Python, build result, and test result. Fix build failures before changing production architecture.
5. Reconcile `LEARNIFY_UI_UX_AUDIT.md` with the current manifest and sources; treat it as historical where claims conflict.

**Gate:** A clean checkout builds the debug Android app and passes both app and backend test suites on a documented machine/CI runner.

### 1. Decide what “version 1” actually promises

Write down the first-release user journeys and what is intentionally out of scope: account creation/sign-in, course browsing, lessons, assessments, AI tutor, payments, admin portal, notifications, and offline operation. Pick a small, testable MVP rather than promising every screen as a production service.

Also decide the launch country/regions, user age range, account support process, data retention, and who owns support and incident response. Because the app is education-focused and may be used by minors, obtain jurisdiction-specific privacy/legal review before collecting real student data.

**Gate:** A product owner approves the MVP, user roles, data list, launch regions, and support/legal owners.

### 2. Remove production blockers from the client and manifest

1. Remove `OPENAI_API_KEY` from Android `BuildConfig` and remove direct provider calls from `GpaCalculatorActivity`. Route AI requests through the authenticated backend. Rotate any provider key that may already have been used in a distributed APK or shared build.
2. Remove the seeded `admin` / `admin123` account from `DatabaseHelper`. Provision the first administrator through a server-side, one-time, audited process; require a unique credential and MFA for privileged accounts where supported.
3. Set the production AI base URL to the real HTTPS API hostname using a deliberate build configuration. Keep `localhost`/emulator loopback only in debug configuration. Do not broaden cleartext exceptions for production.
4. Review every manifest permission against a feature that actually needs it, with runtime consent where Android requires it. In particular, justify contacts, microphone, camera, notifications, exact alarms, and legacy storage access; remove permissions that are not needed for the MVP.
5. Review all `android:exported="true"` activities and receivers. Mark internal components non-exported unless there is a documented external entry point, and validate any deep link. This does not replace server-side access control.
6. Review the release shrinker/ProGuard rules and ensure no debug-only logging or test endpoints ship.

**Gate:** Static review and tests confirm there are no embedded API secrets, no default production admin credentials, no unintended external component entry points, and the release build only communicates over HTTPS.

### 3. Build the production backend and data boundary

The current Spring Boot service is an AI gateway, not yet the complete app backend. Define and implement the server APIs the approved MVP needs:

- Account registration, authentication, session/token renewal, logout/revocation, account recovery, and verification.
- Role and ownership enforcement for student/admin actions on every request.
- Server-side persistence and schema migrations for the shared data that must follow users across devices.
- Explicit input validation, response/error contracts, request-size/time limits, abuse protection, and rate limits (especially AI).
- Secure administrator provisioning and audit trails.
- HTTPS termination, secret management, backups/restore drills, database migrations, health checks, and monitoring.

Keep Gemini/OpenAI credentials exclusively in backend deployment secrets. Restrict CORS to the actual web origins if browser clients need it; Android native requests do not need wildcard browser CORS. Return real HTTP status codes for failures rather than disguising errors as successful answer objects.

The repository has both `com.million.learnify.studentbackend` and `com.myapp.studentbackend` code, plus additional root-package classes. Select one canonical implementation, test the packaged artifact, and remove duplication in a reviewed change.

**Gate:** A deployed staging API has documented OpenAPI contracts, authenticated role-checked endpoints, persistent test data, automated tests, logs/alerts, and no secrets in the repository or client.

### 4. Connect Android to the staging service

1. Replace the `localhost` Retrofit base URL in `ChatViewModel` with the environment-specific HTTPS endpoint.
2. Move authentication and shared records behind the backend. Treat SQLite as an offline cache/local content store, not as the authority for identity, admin access, payments, or cross-device data.
3. Define offline behavior intentionally: what can be read offline, what can be queued, how conflicts resolve, and how the UI shows stale or unsynced data.
4. Add safe token storage/session expiry, logout clearing, and reauthentication. Do not put long-lived privileged secrets in app storage.
5. Use a payment provider's approved mobile flow if payments are in the MVP. The local `payments` SQLite table is not evidence of a real payment processor or verified payment state. Never grant paid access from a client-only status flag.
6. Confirm account deletion, export, and support flows work against the server and follow the selected retention policy.

**Gate:** Student and administrator workflows work against staging on a physical phone, across two devices for one account, and under offline/expired-session/network-failure conditions.

### Payments: current implementation and production gate

The current payment screen offers local manual-transfer instructions, two hardcoded ETB plans, transaction-ID entry, and receipt-image selection. A submission writes its username, transaction ID, amount, local receipt path, status, and timestamp to the device's SQLite `payments` table. The receipt itself is copied into the submitting app's private files. The current Android backup rules include the complete SQLite database, shared preferences, and private files, so receipt images and local account/payment records are included in backup/transfer scope and need an explicit privacy/retention decision. There is no payment API, remote receipt upload, provider checkout, provider settlement verification, or shared administrator queue in the backend. An administrator decision changes the local row and can grant local premium access; it cannot securely update an account on another user's phone.

The product owner confirmed that the displayed CBE/Telebirr recipient accounts are authorized, approved the current 50 ETB (Math/English) and 100 ETB (all-subject lifetime) plans, and selected manual review without automatic provider verification for direct/private distribution. For Play distribution, the selected path is Google Play Billing. The selected backend is Cloud Run, Firebase Authentication, Cloud SQL PostgreSQL, and private Cloud Storage. The owner approved a staged Firebase cutover and online delivery for paid lessons, with free lessons remaining offline. These decisions do not make the existing local flow safe for real collections or establish a verified payment record; payment APIs and cloud resources have not yet been implemented/deployed.

**Implemented first security milestone:** the Spring backend now requires a Firebase-verified, email-verified ID token for every API except `/api/health`; `/api/admin/**` also requires the Firebase `admin=true` custom claim. Configure `LEARNIFY_FIREBASE_AUTH_ENABLED=true` and `FIREBASE_PROJECT_ID` in the environment, and use Cloud Run's service identity/Application Default Credentials—never a service-account key in the app or repository. The default disabled-auth mode fails closed. Six backend integration tests cover public health, anonymous rejection, verified student access, admin authorization, and unverified email.

This milestone is not yet connected to the Android login or assistant client, and there are no payment/content endpoints, Cloud SQL migrations, or receipt-storage implementation yet. The repository contains no `google-services.json`; do not deploy this backend or expect existing Android cloud AI calls to authenticate until the approved Firebase client cutover is implemented and configured locally.

The student screen now states this is a phone-local prototype, and selecting a receipt no longer claims that payment has been verified. These labels are not a production payment workflow. Do not ask real users to transfer money or promise paid access through this flow.

Before implementing live payments:

1. Implement separate Play and direct/private payment paths. Google Play generally requires Play Billing for digital app features/content sold in a Play-distributed app, subject to eligible-country programs and policy exceptions; check the current [Google Play payments policy](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en) for the actual launch case.
2. Implement authenticated server-side order creation, provider-approved Play checkout and purchase verification, idempotent state transitions, protected receipt storage for the manual-transfer channel, admin authorization/audit logs, and server-authoritative entitlements. Keep secrets and payment verification off-device.
3. Test Play Billing sandbox purchases, cancellation, duplicate callbacks, refunds/reconciliation, and access revocation; for direct manual transfers, test receipt upload, admin review, rejection/resubmission, and reconciliation. Never infer settlement from a client-submitted transaction ID or screenshot alone.
4. The product owner approved a staged Firebase account cutover: require verified Firebase email/Google sign-in, preserve existing device-local learning data during transition, provision administrators separately on the server, and manually revalidate old payment claims. This cutover is not implemented. Do not migrate local `Approved` payment rows as trusted entitlements.
5. Decide how paid learning content will be protected. Subject material is bundled in the Android app and local SQLite controls access; a client-side premium check can be modified. Strong server-authoritative access requires paid content to be delivered from authenticated APIs (which affects offline use). Offline copies can be made harder to extract, but cannot be strongly protected on a user-controlled device.

**Gate:** The chosen payment method is allowed for the distribution channel and launch markets; the merchant is approved; sandbox transactions and server-side entitlement tests pass; and the local-only manual flow is removed or explicitly limited to a private demo.

### 5. Make data and educational content safe to operate

1. Inventory every personal/student field and where it is stored, transmitted, logged, backed up, and deleted. Minimize what the MVP collects.
2. Publish accurate privacy and terms pages on domains controlled by the product owner; verify in-app links, support email, and deletion contact.
3. Complete the applicable app-store privacy/data-safety disclosures and obtain qualified review for the launch markets and age group.
4. For AI, document what prompts are sent to the provider, retention and provider settings, safety boundaries, cost limits, and how users report harmful or incorrect content. Avoid sending identifying student details unless necessary and approved.
5. Verify rights/attribution for all bundled subject material, questions, images, libraries, and simulations.
6. Add accessible screen-reader labels, dynamic text-size coverage, contrast checks, locale review, and a clear way to report accessibility or content problems.

**Gate:** The data inventory, privacy disclosures, content-rights record, AI policy, and accessibility checks are approved for the target audience and regions.

### 6. Test the journeys, not only the helpers

Maintain automated unit tests and add device-level integration coverage for:

- New account, verification, sign-in, password recovery, logout, and account deletion.
- Student/admin route separation and direct-launch attempts against privileged screens.
- Course content, quizzes/exams, progress sync, camera/microphone permission denial, and offline access.
- AI success, provider rate limiting, timeout, malformed responses, and empty/error states.
- Payment sandbox completion, cancellation, duplicate callback/idempotency, and reconciliation if payments are included.
- Database upgrades from existing versions (current SQLite version is 57), including preserving user data.
- Accessibility, localization, small/large screens, Android versions, and the supported device/ABI matrix.

Run smoke tests on a physical low/mid-range Android device and an emulator. Measure APK/AAB download size and install size: embedded Python, OpenCV, and 201 bundled learning assets can materially affect both.

**Gate:** Tests pass from a clean checkout; a release candidate has completed the written regression suite on the agreed Android/device matrix with no unresolved release-blocking crash or data-loss defect.

### 7. Add CI, dependency governance, and repeatable builds

Create a GitHub Actions workflow (or the team's CI equivalent) to build and test Gradle and Maven on pull requests, run static checks and dependency/security updates, and retain build/test reports. Use pinned wrapper versions, cache dependencies safely, and keep signing credentials and API secrets in the CI secret store. Generate an SBOM/dependency inventory and define a patch/update process for Android, Maven, Python, and bundled libraries.

**Gate:** A pull request from a clean checkout produces the same tested artifacts without developer-machine paths or credentials.

### Cloud Run deployment status

Google Cloud Run is the selected target, but **no cloud resource has been created and no deployment has been attempted**. The backend now reads Cloud Run's `PORT` environment variable, defaulting to 8080 locally. The Android release build requires a configured HTTPS URL.

Deployment is blocked on safety and tooling. The assistant endpoint currently has no backend authentication, authorization, per-user quota, or rate limiting; if exposed publicly, it would be an unrestricted AI-provider proxy. Do not deploy it with public unauthenticated access until those controls are implemented and tested. The official Java/Spring Boot quickstart requires billing to be enabled; billing was not approved or changed. Cloud Run's free tier is conditional, and usage beyond it, network egress, and Gemini usage can incur separate costs. `africa-south1` (Johannesburg) is only a candidate region to benchmark for Ethiopia, not a confirmed lowest-cost or lowest-latency choice.

Portable JDK 25 and Git were installed under the session's user-profile toolchain folder and archive checksums were verified. The repository's staged and unstaged state was inspected after the edits; unrelated pre-existing changes were left untouched, and no files were staged or committed by this work. The Android SDK configured in `local.properties` is missing, so Android tests still cannot run. `gcloud`, Docker, and ADB are also unavailable. Review the current [Cloud Run Java quickstart](https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service) and [Cloud Run pricing](https://cloud.google.com/run/pricing) again before deployment. No command that enables public access or billing has been run.

### 8. Prepare the release and operate it

1. Confirm the package ID, app name, icon, screenshots, store listing, support contact, privacy policy, and release notes. Increment `versionCode` for each Play release.
2. Configure a protected Android upload/release key outside the repository, with secure backup and restricted access. The checked-in Gradle configuration currently has no release `signingConfig`; do not improvise by committing a keystore/password.
3. Verify the current Play Console target-API requirement and update `targetSdk` accordingly. Test behavior changes introduced by each target-SDK upgrade.
4. Build a signed Android App Bundle (`.aab`) from the tagged release commit; verify the signature, inspect the merged manifest, and test the exact signed artifact.
5. Publish first to internal testing, then closed testing with real testers and support coverage. Use a staged rollout and a documented rollback/hotfix process.
6. Monitor crash-free sessions, API errors/latency, AI spend/rate limits, authentication abuse, and support tickets. Set alerts and an owner for each. Practice restoring the backend database from backup before public launch.

**Gate:** Store review passes; a small cohort completes the real onboarding and support path; monitoring, rollback, incident contacts, and backup restoration are proven before broad availability.

## Firebase authentication milestone (implementation in progress)

The Android client now has Firebase email/password registration and sign-in, Firebase email verification and password-reset links, Google credential exchange when the Firebase web client ID is configured, and server-claim-gated admin sign-in. Existing local student profiles and learning records are preserved during login; old local payment approvals are not promoted to trusted entitlements. The Android assistant client obtains an ID token for protected API requests and attempts one forced refresh after a 401. Sign-out clears the Firebase-backed local session. Password changes and admin-initiated reset actions are routed through Firebase rather than changing only a device-local password.

The Spring API protects all routes other than `/api/health` and `/error`; admin routes require the verified server-issued `admin=true` claim. The Android client cannot grant that claim. The app requests Firebase verification before establishing student/admin sessions.

This is **not yet verified or ready for real users**:

- No `app/google-services.json` is present, so the configured Firebase Android project, package match, Firebase providers, OAuth client ID, and live auth flows have not been verified. Add the Firebase configuration locally for `com.million.learnify`; never commit the file or print its contents.
- Android tests and debug build remain blocked because the SDK path in `local.properties` does not exist. Install Android SDK Platform 36 and platform-tools, then rerun the Android unit tests and debug build.
- Debug now targets local `http://localhost:8080/` unless explicitly overridden; a physical phone needs `adb reverse tcp:8080 tcp:8080` to reach the development host. Release has no default API host, and the release URL guard was verified to reject an unset `LEARNIFY_API_BASE_URL`.
- The backend Maven suite passes **33/33 tests** after the current auth changes, including missing/malformed/invalid/expired/revoked token handling, verified-email rejection, student/admin authorization, and public health access. This uses a mocked token verifier; no Firebase emulator or real Firebase project test has been run, so registration, Google sign-in, verification, reset, revocation, and custom admin claim provisioning still require controlled integration testing.
- Android unit tests now cover verified-email/admin-claim policy and bearer-token attachment, refresh/retry, and sign-out behavior. They have not run because the Android SDK is missing; no successful Firebase login or physical-device flow has been verified.
- The local SQLite user and learning-data model remains device-local. The authenticated AI endpoint does not yet provide account synchronization or production entitlements, and no backend deployment or payment transaction has been performed.
- The current Android-only account deletion and deactivation flows do not delete Firebase identities or backend data. Do not present those actions as complete cloud-account lifecycle controls until a server-authoritative deletion flow exists.

### Next release gates

1. Add a locally managed Firebase Android configuration matching `com.million.learnify`; enable email/password and Google providers and configure the approved OAuth clients.
2. Install Android SDK Platform 36 and platform-tools, then pass Android unit tests, debug build, and device-level sign-in/verification/Google/reset/expired-token/AI-auth tests.
3. Pass the backend test suite, deploy a private staging service over HTTPS, configure Firebase Admin credentials through the hosting secret manager, and provision admin claims only through an audited trusted operator process.
4. Implement PostgreSQL migrations and authenticated account/data APIs; test server-side access controls, backups, deletion, and account recovery before replacing local-only records.
5. Build the payment system as a separate milestone: private receipt storage and manual-review APIs for direct CBE/Telebirr, Google Play purchase-token verification for Play distribution, server-authoritative paid-content entitlements, and authenticated online lesson delivery. Do not accept real money until these workflows and enforcement have passed security and end-to-end tests.

## Immediate next actions

1. **Complete Firebase setup and Android build validation** with the missing `google-services.json`, Android SDK Platform 36, and platform-tools; run the Android unit tests and debug build on a configured machine.
2. **Deploy only a private staging backend over HTTPS** after backend tests pass; never use a public unauthenticated assistant endpoint.
3. **Test the actual phone flows** for email/password, verification, Google sign-in, password reset, token refresh/sign-out, and AI requests; record the device/API results.
4. **Design the authenticated PostgreSQL-backed account/data API** and an audited admin-claim provisioning procedure.
5. **Keep payments disabled** until receipt storage/review, Play verification, server-side entitlements, and paid-content delivery are implemented and tested end to end.

## Verification notes and limitations

- The repository was inspected on 21 October 2026. Source inventory shows 109 Android Java files, 108 layout XML resources, 117 interactive-lab assets, 84 subject assets, three Python modules, and separate Android and Spring Boot test trees.
- `ChatViewModel` now uses the configured `LEARNIFY_API_BASE_URL`, not a hardcoded backend address. The network security configuration permits cleartext only for local development loopback hosts.
- The initial Android code embedded an OpenAI key field and called the provider directly from GPA advice. The first implementation batch removed that field and direct call, routed GPA advice through the Spring assistant endpoint, and removed the unused Gemini Android client dependency.
- Android AI base URLs are now variant-specific: debug defaults to `http://localhost:8080/` for a physical device using `adb reverse tcp:8080 tcp:8080`; an emulator can use `-PLEARNIFY_DEBUG_API_BASE_URL=http://10.0.2.2:8080/`. Release reads `LEARNIFY_API_BASE_URL` from a Gradle project property or environment variable and refuses to assemble an APK/AAB unless it is a non-local HTTPS URL ending in `/`. This is not a deployed endpoint; no production URL has been supplied or deployed.
- Official Gemini model documentation currently lists `gemini-3.5-flash-lite` as a stable model. This review did not verify the configured `gemini-flash-lite-latest` alias against the user's AI Studio project/account, and no sanitized connection-reset logs were provided, so the reported provider reset cause remains unconfirmed. Do not change the production model without a request test using the actual account and a verified quota/cost policy. See [Gemini models](https://ai.google.dev/gemini-api/docs/models) and [Gemini API troubleshooting](https://ai.google.dev/gemini-api/docs/troubleshooting).
- The backend configuration reads `GEMINI_API_KEY` from environment-backed configuration, which is the correct general direction, but the service still needs production authentication, abuse controls, deployment configuration, and operational safeguards.
- The backend HTTP port now honors `PORT`; the wildcard CORS annotation was removed from the Android-only assistant controller. Neither change authenticates or rate-limits requests, so the service remains unsafe for public exposure.
- The existing UI audit records historical physical-device and test results. Some findings are stale relative to the current manifest, so rerun checks on the present checkout.
- Backend verification: `.\mvnw.cmd test` passed all 33 tests on the verified portable JDK 25 after the Firebase security tests were extended. Android verification remains blocked: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` stops because the `sdk.dir` configured in `local.properties` points to a directory that does not exist and `ANDROID_HOME`/`ANDROID_SDK_ROOT` are unset. No Android tests, physical-phone test, release build, or Play Console submission has been verified. Passing those gates remains outstanding.
