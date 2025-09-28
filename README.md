![Backend CI](https://github.com/ArchILLtect/sameboat-backend/actions/workflows/backend-ci.yml/badge.svg)
# SameBoat Backend (Spring Boot + Java 21)

> Quick Links: [Instructions (setup & migrations)](./docs/instructions.md) | [API Reference](./docs/api.md) | [Week 3 Plan](./docs/Weekly%20Plan%20Docs/week_3_plan_same_boat_honors_project_fall_2025.md) | Journals: [Index](./docs/journals/README.md) · [Week 1](./docs/journals/Week1-Journal.md) · [Week 2](./docs/journals/Week2-Journal.md)

## Run locally
1. Copy `.env.example` → `.env` and fill appropriate JDBC DS values (or rely on defaults in `application.yml`).
2. Or set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` directly.
3. Start (Dev):
   ```bash
   ./mvnw spring-boot:run
   ```
4. Health check: `GET /health` → 200 OK

### Profiles
- `default` (local dev): insecure cookie (no `Secure` attribute), domain unset, session TTL 7 days.
- `prod`: `Secure` cookie, domain `.sameboat.<tld>`, session TTL 14 days, different allowed CORS origins.
  ```bash
  ./mvnw spring-boot:run -Dspring.profiles.active=prod
  ```

## Configuration Properties
Type‑safe properties (see `SameboatProperties`) under prefix `sameboat.*`:

| Property | Type | Default (dev) | Prod Override | Description |
|----------|------|---------------|---------------|-------------|
| sameboat.auth.dev-auto-create | boolean | false | (same) | Auto-create user on first login if password matches stub (dev convenience) |
| sameboat.auth.stub-password | string | dev | (same) | Stub password used for auto-create path (removed in real prod) |
| sameboat.cookie.secure | boolean | false | true | Sets `Secure` attribute on session cookie |
| sameboat.cookie.domain | string | (blank) | .sameboat.<tld> | Cookie domain (blank = omit attribute) |
| sameboat.session.ttl-days | int | 7 | 14 | Session lifespan (days) |
| sameboat.cors.allowed-origins | list | http://localhost:5173,5174 | app+preview origins | Allowed browser origins (CORS) |

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

Pre-commit hook (optional):
1. Run: `git config core.hooksPath .githooks`
2. The provided `.githooks/pre-commit` executes the check; commit aborts if an existing migration is modified.

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
Current codes: `UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`, `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR`.

| Code | Typical Trigger |
|------|-----------------|
| UNAUTHENTICATED | Missing / invalid / garbage session cookie |
| BAD_CREDENTIALS | Wrong email/password at login |
| SESSION_EXPIRED | Session present but past expiry time |
| EMAIL_EXISTS | Duplicate registration attempt |
| VALIDATION_ERROR | Bean validation (fields, sizes, empty patch body) |
| BAD_REQUEST | Explicit IllegalArgument / future semantics |
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

## Sample cURL
```bash
# Register
curl -i -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"dev@example.com","password":"abcdef","displayName":"Dev"}'

# Login
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"dev@example.com","password":"abcdef"}'

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
