# Test Automation Plan

Automatic tests are feasible for this project, but not as a single layer.

The practical target is a layered suite:

- Unit tests for pure helpers and isolated controller decisions.
- Integration tests for Hibernate mappings and database-backed workflows.
- HTTP or browser tests for login, account creation, confirmation, reset password, protected-page access, regatta editing, and complaint links.
- Deployment smoke tests for server boot, WAR deploy, and health checks.

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
- a repeatable local start script for GlassFish 6 + database + deploy.

## First Slice To Build

The first automated slice should prove the pipeline end to end:

1. Unit tests for controller login/logout and anonymous-access decisions.
2. Integration tests for participant save/load and regatta/registration persistence.
3. Browser flow for `welcome -> login -> create account -> confirm mail -> login`.
4. Browser flow for protected-page redirects and complaint-link entry points.

## Current Blockers

- No `src/test` tree exists yet.
- `pom.xml` has no JUnit, Mockito, Surefire, or Failsafe configuration.
- No Maven wrapper is present, so automatic execution depends on machine-specific Maven setup.
- Mail sending is tied to live Gmail OAuth credentials.
- JSF request beans depend on container state instead of injected adapters.

## Local Execution On This Machine

The first local test entry point is:

- `powershell -ExecutionPolicy Bypass -File scripts/test-local.ps1`

The script looks first for the Maven installation already present on this computer and falls back to `mvn` in `PATH` if available.

Available local modes:

- `unit`
- `verify-live`
- `deploy-and-verify-live`
- `browser-live`
- `deploy-and-browser-live`
- `full-live`

## Conclusion

Yes, tests can be designed for all major aspects of the application, but the suite should be built in layers and started from controller, persistence, and critical user journeys first.
