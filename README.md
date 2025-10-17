![Backend CI](https://github.com/sameboat-platform/backend/actions/workflows/backend-ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-JaCoCo%20%E2%89%A570%25-brightgreen)
[![Contributing](https://img.shields.io/badge/guide-CONTRIBUTING.md-blue)](./CONTRIBUTING.md) [![Guard Rails](https://img.shields.io/badge/AI%20Guard%20Rails-copilot--instructions.md-purple)](.github/copilot-instructions.md)
# SameBoat Backend (Spring Boot + Java 21)

> Repository migration: moved from `ArchILLtect/*` to `sameboat-platform/*` on 2025-09-28. Update any local remotes:
> ```bash
> git remote set-url origin git@github.com:sameboat-platform/sameboat-backend.git
> ```

> Quick Links: [Instructions (setup & migrations)](./docs/instructions.md) | [API Reference](./docs/api.md) | [Risks](./docs/RISKS.md) | [JWT vs Extended Sessions (Spike)](./docs/spikes/jwt_vs_extended_sessions.md) | [Week 3 Plan](./docs/weekly-plan/week-3/week-3-plan.md) | [Contributing](./CONTRIBUTING.md) | Guard Rails: [Copilot Instructions](.github/copilot-instructions.md) | Journals: [Index](./docs/journals/README.md) · [Week 1](./docs/journals/Week1-Journal.md) · [Week 2](./docs/journals/Week2-Journal.md)

## Getting Started (Contributors)
Before writing code or using AI assistance:
1. Open `CONTRIBUTING.md` AND `.github/copilot-instructions.md` in your IDE (keep both tabs open so Copilot can ingest context).
2. For every Copilot / AI prompt, prepend:
   ```
   Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.
   ```
3. If Copilot cannot “see” `.github/workflows/backend-ci.yml` it MUST reply exactly:
   `backend-ci.yml is missing. Please confirm before I generate any new CI workflow.` – fix indexing before proceeding.
4. Never add another workflow file while `backend-ci.yml` exists.
5. Add tests (unit + integration) for all new service methods or endpoints.
6. Create new Flyway migrations—never edit historical ones.

Alias Tokens (for AI prompts): `BACKEND_CI_GUARD`, `LAYER_RULE`, `SECURITY_BASELINE` – always prepend them in complex generation tasks.

## Run locally
1. Copy `.env.example` → `.env` and fill appropriate JDBC DS values (or rely on defaults in `application.yml`).
2. Or set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` directly.
3. Start (Dev):
   ```bash
   ./mvnw spring-boot:run
   ```
4. Health checks (both are public):
   - `GET /health` → 200 OK (custom simple health)
   - `GET /actuator/health` → 200 OK and `{ "status": "UP" }`

### Container (Docker) Usage
A multi-stage Dockerfile is provided for local testing and Render deployment.

Build image (local):
```bash
docker build -t sameboat-backend .
```
Run locally (expose port 8080):
```bash
docker run --rm -e PORT=8080 -p 8080:8080 sameboat-backend
```
Override datasource via env vars (example Neon):
```bash
docker run --rm \
  -e PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org \
  -e SAMEBOAT_COOKIE_SECURE=true \
  -e SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboatplatform.org \
  -p 8080:8080 sameboat-backend
```
The container entrypoint honors Render's provided `$PORT` environment variable; locally you must supply `-e PORT=<port>` (default not hardcoded). JVM memory tuned via `JAVA_OPTS=-XX:MaxRAMPercentage=75.0` (adjust if needed for constrained instances).

For slimmer production images you can optionally rebuild using a distroless or alpine JRE base; keep current Eclipse Temurin JRE for consistency unless a size optimization is required.

### Profiles
- `default` (local dev): insecure cookie (no `Secure` attribute), domain unset, session TTL 7 days.
- `prod`: `Secure` cookie, domain `.sameboatplatform.org`, session TTL 14 days, CORS allowlist includes `https://app.sameboatplatform.org`.
  ```bash
  ./mvnw spring-boot:run -Dspring.profiles.active=prod
  ```

### Deployment & Hosting (MVP Plan)
| Component | Provider | Domain / Endpoint | Notes |
|-----------|----------|-------------------|-------|
| Backend API | Render (Web Service) | https://api.sameboatplatform.org | Deployed from `main` (container or buildpack). Scale later. |
| Frontend App | Netlify (React/Vite) | https://app.sameboatplatform.org | Consumes API at `api.` subdomain. |
| Root Domains | DNS (Registrar) | sameboatplatform.org / .com | `.com` 301 → `.org`. Wildcard / APEX redirect as needed. |
| Database | Neon Postgres | Managed cluster (TLS) | Use `sslmode=require` in JDBC. |
| Auth Cookie | Browser (SBSESSION) | Domain: `.sameboatplatform.org` | `Secure`, `HttpOnly`, `SameSite=Lax/Strict` (verify config). |
| CORS Policy | Spring Config | Allow Origin: `https://app.sameboatplatform.org` | Credentials (cookies) enabled. |

Environment variable examples (Render) for Neon (replace placeholders):
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
SPRING_PROFILES_ACTIVE=prod
SAMEBOAT_COOKIE_SECURE=true
SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org
SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboatplatform.org
```

Base API URL (prod): `https://api.sameboatplatform.org`
Frontend Origin: `https://app.sameboatplatform.org`

If you add a new externally accessible endpoint, update `openapi/sameboat.yaml` and, if needed, note any new CORS origins (keep the list minimal).

## Configuration Properties
Type‑safe properties (see `SameboatProperties`) under prefix `sameboat.*`:

| Property | Type | Default (dev) | Prod Override | Description |
|----------|------|---------------|---------------|-------------|
| sameboat.auth.dev-auto-create | boolean | false | (same) | Auto-create user on first login if password matches stub (dev convenience) |
| sameboat.auth.stub-password | string | dev | (same) | Stub password used for auto-create path (removed in real prod) |
| sameboat.cookie.secure | boolean | false | true | Sets `Secure` attribute on session cookie |
| sameboat.cookie.domain | string | (blank) | .sameboatplatform.org | Cookie domain (blank = omit attribute) |
| sameboat.session.ttl-days | int | 7 | 14 | Session lifespan (days) |
| sameboat.cors.allowed-origins | list | http://localhost:5173,5174 | https://app.sameboatplatform.org | Allowed browser origins (CORS) |

Override at runtime via environment variables (Spring relaxed binding). Examples:
```bash
export SAMEBOAT_SESSION_TTL_DAYS=3
export SAMEBOAT_COOKIE_SECURE=true
./mvnw spring-boot:run
```

## Migrations
Flyway runs automatically on startup from `src/main/resources/db/migration`.

### Migration Immutability Policy
Once a migration version (e.g. `V1__*.sql`, `V2__*.sql`) has been merged to `main` and applied to any environment, **do not edit the file**. If schema changes are required, create a new migration with the next version (e.g. `V3__add_new_column.sql`). This preserves checksum integrity and avoids drift. If a previous file was accidentally changed and Flyway reports a checksum mismatch, prefer creating a corrective follow-up migration over editing history; use `flyway:repair` only after confirming the live schema truly matches the intended SQL.

### Legacy Alignment
`V3__users_additional_columns.sql` exists to reconcile differences between the original `V1` schema and the expanded fields/constraints added conceptually in `V2` without rewriting history. New adjustments must continue with `V4+`.

### Immutability Tooling
Scripts (cross‑platform) enforce the "no edits to applied migrations" rule:

| Script | Platform | Usage |
|--------|----------|-------|
| `scripts/check-migration-immutability.sh` | CI / Bash | `scripts/check-migration-immutability.sh origin/main` |
| `scripts/check-migration-immutability.ps1` | PowerShell | `pwsh scripts/check-migration-immutability.ps1 -BaseRef origin/main` |
| `scripts/check-migration-immutability.cmd` | Windows (cmd shim) | `scripts\check-migration-immutability.cmd` |

### Enable shared Git hooks (once per clone)
- macOS/Linux: `./scripts/enable-git-hooks.sh`
- Windows (PowerShell): `pwsh scripts/enable-git-hooks.ps1`

CI: The GitHub Actions workflow runs the Bash script before tests; it fails the build if a historical migration was altered.

## CI Pipeline (Current)
1. Checkout (full history: `fetch-depth: 0`).
2. Make scripts executable.
3. Migration immutability check (fails fast if an applied migration file was modified). For PRs uses `GITHUB_BASE_REF`; for pushes auto-detects remote default branch.
4. Maven `verify` (regular unit + integration tests; migration container test skipped by default).
5. Migration schema test profile: `mvn -Pwith-migration-test test` runs Testcontainers-based Flyway schema verification.

### Manual CI Parity Commands
```
# Regular tests (skip migration schema test)
mvn verify

# Include migration schema verification
mvn -Pwith-migration-test test

# Immutability check locally (auto base)
scripts/check-migration-immutability.sh
# Against specific base
scripts/check-migration-immutability.sh origin/main
```

### Flyway Maven Plugin (Defaults & Overrides)
Flyway plugin uses fallback properties defined in `pom.xml`:
- Default URL: `jdbc:postgresql://localhost:5432/sameboat`
- Default user/password: `postgres / postgres`
Override at runtime:
```
./mvnw -Dflyway.url=jdbc:postgresql://HOST:5432/DB -Dflyway.user=USER -Dflyway.password=PASS flyway:info
```
If you prefer using Spring datasource env vars, run the application instead of the plugin (app Flyway auto-migration uses `application.yml` variable layering).

---

## 📅 Project Schedule
See the [Semester Project Plan](./schedule/SemesterPlan.md) for calendar files, screenshots, and live links.

---
## Authentication & Sessions
Opaque session cookie `SBSESSION=<UUID>` (alias accepted: `sb_session`) issued on **login** or **register**. Include it as a normal Cookie header for authenticated endpoints (`/me`, `PATCH /me`).

### Registration
`POST /auth/register` (also `/api/auth/register`): returns `{ "userId": "<uuid>" }` and sets cookie.

### Login
`POST /auth/login` (also `/api/auth/login`): returns full user envelope `{ "user": { ... } }`.
- If `sameboat.auth.dev-auto-create=true` (test/dev), a non‑existent user with stub password is auto-created.
- Passwords are stored with **BCrypt** (Spring `BCryptPasswordEncoder`).
- Password complexity enforced: min 8 chars, must include upper, lower, and digit.
- Login attempts are rate limited; excessive failures return `RATE_LIMITED` (HTTP 429).

### Logout
`POST /auth/logout` clears server session and sends an expired cookie.

### Expiration
Session TTL: 7 days dev / 14 days prod. Expired requests → error code `SESSION_EXPIRED`.

See full contract & examples in [API Reference](./docs/api.md).

## Error Envelope
All non-2xx errors:
```json
{ "error": "<CODE>", "message": "Human readable explanation" }
```
Current codes: `UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`, `VALIDATION_ERROR`, `BAD_REQUEST`, `RATE_LIMITED`, `INTERNAL_ERROR`.

| Code | Typical Trigger |
|------|-----------------|
| UNAUTHENTICATED | Missing / invalid / garbage session cookie |
| BAD_CREDENTIALS | Wrong email/password at login |
| SESSION_EXPIRED | Session present but past expiry time |
| EMAIL_EXISTS | Duplicate registration attempt |
| VALIDATION_ERROR | Bean validation (fields, sizes, empty patch body) |
| BAD_REQUEST | Explicit IllegalArgument / future semantics |
| RATE_LIMITED | Too many requests (e.g., repeated failed logins) |
| INTERNAL_ERROR | Uncaught exception (trace id logged) |

## Quality Gates
| Gate | Status | Notes |
|------|--------|-------|
| Migration immutability | Enforced in CI | Fails build if applied migration edited |
| Test suite | Required | `mvn verify` runs unit + integration tests |
| Coverage threshold | >= 70% instructions | JaCoCo gate (ratchet forward) |
| Schema verification (Flyway) | Optional profile | `-Pwith-migration-test` uses Testcontainers |

## Roadmap (Next Auth Steps)
- Refresh tokens or sliding session extension
- Idle timeout & session rotation
- Password reset flow (email / token)
- Role-based authorization (admin, moderator)
- Central OpenAPI (`openapi/sameboat.yaml`) sync
- Client-friendly error detail localizations

## Versioning & Continuous Delivery

This project uses [Semantic Versioning](https://semver.org/) (v0.1.0, v0.2.0, v1.0.0, etc.) for all releases. Version tags are created and pushed to GitHub (see below).

### Release Process
1. Bump the version in `pom.xml` (e.g., to 0.2.0).
2. Create an annotated tag: `git tag -a v0.2.0 -m "Release v0.2.0"`
3. Push the tag: `git push origin v0.2.0`
4. The CI workflow (`backend-ci.yml`) will build, test, and publish the backend JAR to GitHub Releases.
5. Deployment to Render/Docker is triggered by new tags/releases (see Render docs).
6. Verify deployment by calling `GET /api/version` on the deployed backend.

### Milestones & Project Board
- Track release progress and issues in GitHub milestones (e.g., v0.2.0) and the project board.

### Onboarding Checklist
- Review CONTRIBUTING.md and docs/instructions.md for setup and release steps.
- Use the /api/version endpoint to confirm deployed version.

### Verification (smoke)
Use any HTTP client or browser:
- `GET /actuator/health` → should return 200 + `{ "status": "UP" }` (public)
- `GET /api/version` → should return 200 + `{ "version": "<semver>" }` (public)

## Sample cURL
```bash
# Register (password must include upper, lower, digit and be >= 8 chars)
curl -i -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"dev@example.com","password":"Abcdef12","displayName":"Dev"}'

# Login
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"dev@example.com","password":"Abcdef12"}'

# Use cookie
curl -i http://localhost:8080/me -H 'Cookie: SBSESSION=<uuid>'

# Patch
curl -i -X PATCH http://localhost:8080/me \
  -H 'Content-Type: application/json' \
  -H 'Cookie: SBSESSION=<uuid>' \
  -d '{"displayName":"New Display"}'

# Logout
curl -i -X POST http://localhost:8080/auth/logout -H 'Cookie: SBSESSION=<uuid>'
```

---
For broader architecture overview see `docs/Architecture.md`.

---
## Error Code Catalog Reference
The authoritative list of stable error codes and guidance for adding new domain exceptions lives in [`.github/copilot-instructions.md` – Section 21](.github/copilot-instructions.md#21-standard-exception--error-code-catalog). Review it before introducing a new error or exception handler to ensure consistency and avoid code drift.

If adding a new code:
1. Create a lightweight runtime exception (e.g. `ResourceNotFoundException`).
2. Map it in `GlobalExceptionHandler` with proper HTTP status.
3. Add tests (positive + negative path) asserting `{ "error": "<CODE>" }` envelope.
4. Append the new code (do not repurpose existing ones).

---
## AI Guard Rails (Alias Tokens)
When using Copilot or any AI assistant, prepend prompts with:
```
Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.
```
Meaning:
- BACKEND_CI_GUARD: Never create a new workflow if `backend-ci.yml` exists; if not visible respond with the exact missing-file phrase.
- LAYER_RULE: Maintain controller → service → repository boundaries.
- SECURITY_BASELINE: Validate inputs, avoid leaking sensitive data, enforce least privilege, add tests.

These tokens map directly to detailed guidance in `.github/copilot-instructions.md`. Keeping them explicit reduces accidental violations when IntelliJ indexing is incomplete.

---
<!-- End README enhancements: contributing link, coverage badge, alias tokens note, error catalog reference, deployment & hosting section -->

## Quick Windows check for active profile
```cmd
curl.exe -i http://localhost:8080/actuator/env | findstr /I active
curl.exe -i http://localhost:8080/actuator/env | findstr /I sameboat
```
