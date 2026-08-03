# Test Automation Plan

Automatic tests are feasible for this project, but not as a single layer.

The practical target is a layered suite:

- Unit tests for pure helpers and isolated controller decisions.
- Integration tests for Hibernate mappings and database-backed workflows.
- HTTP or browser tests for login, account creation, confirmation, reset password, protected-page access, regatta editing, and complaint links.
- Deployment smoke tests for server boot, WAR deploy, and health checks.

## Methodology

Each automated test should verify both:

- the visible outcome of the flow
- the final state the system is left in

In this application, a flow is not fully covered just because navigation succeeds.
The test should also assert the postconditions that matter underneath:

- `logout` invalidates the real HTTP session, not only the visible UI state
- `logout -> login` leaves a single active account context in the browser session
- after switching accounts, session-scoped state belongs only to the present account
- protected pages do not inherit stale objects from a previous user
- cleanup actions leave the server stable for the next test

This became a formal rule after the session-accumulation bug: the browser suite exposed that a logout could look correct in the UI while still leaving orphaned server state behind.

## What Is Easy Today

These areas can be covered with low refactor cost:

- `Model/Crypto.java`
- `Model/PasswordGenerator.java`
- `View/NavigationUtil.java`
- `View/PlaceholderFactory.java`
- enum/status rules in `Controller/RegattaStatus.java` and `Controller/RegistrationStatus.java`

## What Needs Small Refactors

These areas are testable, but the current seams are weak:

- `Controller/Controller.java`
  - `newSession(ModelForView)` casts to `ModelBean`, which blocks clean doubles.
  - First step: depend on `ModelForView` or a narrower interface throughout.
- request beans such as `View/ComplaintBean.java`, `View/ConfirmParticipantMailBean.java`, and `View/ResetPasswordBean.java`
  - they read static `FacesContext` directly in `@PostConstruct`.
  - First step: extract request-parameter access behind a small adapter.
- `View/MailerAgent.java`
  - it calls Gmail OAuth and HTTP directly.
  - First step: inject a `MailGateway` so tests can capture outgoing messages without network calls.

## What Needs Environment Support

Some coverage should run against disposable infrastructure:

- `Model/ModelBean.java`
- `Model/U_HibernateUtil.java`
- Hibernate mapping files in `src/main/java/Tables/*.hbm.xml`

Recommended approach:

- JUnit 5 + Mockito for unit tests.
- Testcontainers MySQL for repository/integration tests.
- Playwright for end-to-end browser flows against a deployed WAR.
- a repeatable local start script for GlassFish 7 + isolated test database + deploy.

## Current Regression Priorities

1. authentication, logout, and account switching
2. complaint and mail-link entry points
3. regatta status transitions and recalculation
4. flows that create or reuse session-scoped working objects
5. browser paths that traverse multiple legacy editors

## Remaining Gaps

- Mail delivery itself is still tied to live Gmail OAuth credentials.
- JSF request beans still depend heavily on container state and are easier to cover through live tests than isolated unit tests.
- Administrative coverage is broad but not exhaustive across every screen.
- Local automation still depends on the machine-specific GlassFish and database setup because there is no disposable stack or Maven wrapper yet.

## Local Execution On This Machine

The first local test entry point is:

- `powershell -ExecutionPolicy Bypass -File scripts/test-local.ps1`

The script looks first for the Maven installation already present on this computer and falls back to `mvn` in `PATH` if available.
For live and browser modes, it prefers `TOPRACING_ASADMIN`, then `TOPRACING_GLASSFISH_HOME`, then the documented GlassFish 7 installation.
The default GlassFish domain is `topracing`; override it with `TOPRACING_GF_DOMAIN` only when intentionally testing another domain.

Available local modes:

- `unit`
- `verify-live`
- `deploy-and-verify-live`
- `browser-live`
- `deploy-and-browser-live`
- `full-live`

## Local Isolated Database

Live and browser-oriented local runs now use an isolated MySQL database by default:

- default isolated catalog: `topracing26_test`
- bootstrap script: `scripts/prepare-test-db.ps1`
- browser fixtures also default to that isolated catalog through `scripts/browser-fixture.ps1`

Manual checks use the real local catalog `topracing26`.
Automated live/browser checks recreate `topracing26_test` from `topracing26` and then write temporary participants, regattas, cars, venues, variants, registrations, bids, penalties, and points counts only in the isolated catalog.

This matters because regatta recalculation cost depends heavily on how many historic regattas exist in the database.
The shared local `topracing26` database had accumulated hundreds of regattas and synthetic fixtures, which made some browser tests spend most of their time recalculating unrelated history and polluted manual checks.

The isolated test catalog keeps reference data but starts with no historic regattas, registrations, bids, penalties, or points counts.
That makes status-transition and `editregattaresults.xhtml` tests fast enough to be practical locally.

## Conclusion

Yes, tests can be designed for all major aspects of the application, but the suite should be built in layers and started from controller, persistence, and critical user journeys first.
