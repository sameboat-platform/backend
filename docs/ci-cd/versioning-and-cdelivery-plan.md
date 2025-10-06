# Backend Versioning + Continuous Delivery (CD) Plan

Applies BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE; follows project conventions.

Goals
- Establish clear versioning for the backend artifact (pom.xml) using Semantic Versioning (SemVer).
- Keep public API stable for the SPA; avoid breaking changes to /api during Week 3.
- Move deployment to a Continuous Delivery flow (auto-deploy on green main via Render), with guardrails.
- Preserve Flyway immutability and 70% Jacoco coverage gate.

Constraints & Guard Rails
- BACKEND_CI_GUARD: If a CI workflow exists, modify only .github/workflows/backend-ci.yml. Do not add new workflows without explicit confirmation. If the CI file seems missing, manually verify before generating anything.
- LAYER_RULE: Controller → Service → Repository. Tests for new logic are required.
- SECURITY_BASELINE: Validate DTOs, least-privilege repos, no secrets in logs.
- Flyway: Never edit applied migrations; add new V<N>__*.sql.

Versioning Model
- Artifact Version (Backend service):
  - Use SemVer (MAJOR.MINOR.PATCH) in pom.xml <version>.
  - Week 3: set to 0.1.0 now; if you cut a release at end of the week, bump to 0.2.0.
  - Tag source as v0.1.0, v0.2.0, etc. Keep CHANGELOG notes in GitHub Releases or docs/.
- API Versioning (HTTP endpoints):
  - Continue with /api base path without an explicit /v1 segment for now.
  - Avoid breaking API changes. If a breaking change is unavoidable in the future, introduce a new versioned path (e.g., /api/v1) and deprecate old routes with overlap.

Branching & Release Strategy
- Branching: trunk-based with short-lived feature branches (feat/<short-desc>), PRs into main.
- Release: merge to main = deploy (CD). Protect main with required checks: build, tests, coverage.
- Tagging: create a Git tag (vX.Y.Z) on main after merge. Optionally create a GitHub Release with notes.

Environments & Profiles
- Dev profile (SPRING_PROFILES_ACTIVE=dev):
  - CORS allow http://localhost:5173.
  - Cookie Secure=false; Cookie domain omitted (host-only for localhost).
  - Suitable for local development; use local Postgres or defaults per application.yml.
- Prod profile (SPRING_PROFILES_ACTIVE=prod):
  - CORS allow https://app.sameboatplatform.org (and explicitly listed prod domains).
  - Cookie Secure=true; Cookie domain=.sameboatplatform.org.
  - Actuator base path /api/actuator (only /health and /info exposed and permitted).

CI/CD Pipeline (Conceptual)
- Build: mvn -B -ntp clean verify (runs tests + coverage). Coverage gate must pass (>= 70%).
- Package: Spring Boot jar; optionally a Docker image.
- Migrations: Flyway runs on app startup. Treat each migration as final; roll forward only.
- Deploy: Render auto-deploys from main branch. Keep environment variables limited to datasource and SPRING_PROFILES_ACTIVE.
- Verify: smoke test /api/actuator/health and /api/actuator/info; test an auth round-trip from the SPA origin with credentials: 'include'.

Rollback & Recovery
- Code rollback: revert the offending commit; Render deploys previous version automatically.
- Database: DO NOT modify applied migrations. If a migration caused a problem, ship a forward fix migration (e.g., V<N+1>__revert_or_fix.sql).
- Observability: review logs on Render; avoid logging PII or secrets.

Acceptance Criteria for CD Readiness
- All tests green; coverage gate met.
- main branch protected with required checks.
- Render configured to auto-deploy on push to main.
- SPRING_PROFILES_ACTIVE=prod set on Render; only datasource env vars present.
- /api/actuator/info reachable unauthenticated; all other endpoints require auth except /api/auth/login|register|logout.

Render Environment Variables Policy
- Keep: SPRING_PROFILES_ACTIVE=prod, SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD.
- Remove (now handled by application-prod.yml): SAMEBOAT_COOKIE_DOMAIN, SAMEBOAT_COOKIE_SECURE, SAMEBOAT_CORS_ALLOWED_ORIGINS, SAMEBOAT_SESSION_TTL_DAYS.

API Stability Notes
- Error envelope: { "error": "<CODE>", "message": "<detail>" } is stable. Expected codes include BAD_CREDENTIALS, UNAUTHENTICATED, SESSION_EXPIRED, EMAIL_EXISTS, VALIDATION_ERROR.
- /api/me requires auth; returns 401 with UNAUTHENTICATED or SESSION_EXPIRED otherwise.

Release Notes Template (copy/paste)
- Version: vX.Y.Z
- Date: YYYY-MM-DD
- Changes:
  - Added/Fixed/Removed
- Database migrations: V<N>__description.sql applied
- Deployment notes: any manual steps
- API: breaking/behavioral changes (should be none for minor/patch)

