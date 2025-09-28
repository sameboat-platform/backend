![Backend CI](https://github.com/ArchILLtect/sameboat-backend/actions/workflows/backend-ci.yml/badge.svg)
# SameBoat Backend (Spring Boot + Java 21)

> Quick Links: [Instructions (setup & migrations)](./docs/instructions.md) | [API Reference](./docs/api.md)

## Run locally
1. Copy `.env.example` → `.env` and fill appropriate JDBC DS values.
2. Or set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` directly.
3. Start:
   ```bash
   DB_URL=... ./mvnw spring-boot:run
    ```
4. Health check: GET /health → ok

## Migrations
Flyway runs automatically on startup from src/main/resources/db/migration.

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
## Authentication & Sessions (Week 2 Slice)
Opaque session cookie `SBSESSION` (UUID) returned by `POST /auth/login` (dev stub password: `dev`). Include it as a regular Cookie header for authenticated calls (`/me`, `PATCH /me`). Logout invalidates the session.

See full endpoint contract & examples in [API Reference](./docs/api.md).

## Error Envelope
All non-2xx errors return:
```json
{ "error": "<CODE>", "message": "Human readable explanation" }
```
Current codes: `UNAUTHORIZED`, `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR`.

## Quality Gates
| Gate | Status | Notes |
|------|--------|-------|
| Migration immutability | Enforced in CI | Fails build if applied migration edited |
| Test suite | Required | `mvn verify` runs unit + integration tests |
| Coverage threshold | >= 60% instructions | JaCoCo gate (`pom.xml`) – ratchet later |
| Schema verification (Flyway) | Optional profile | `-Pwith-migration-test` uses Testcontainers |

Raise the coverage bar incrementally (e.g. 60% → 70%) after adding password hashing & additional domain logic.

## Roadmap (Auth Next Steps)
- Replace stub password with hashed credentials & registration flow
- Secure / SameSite=None cookies under HTTPS
- Session rotation & idle timeout
- Role-based authorization annotations
- Merge endpoints into OpenAPI `openapi/sameboat.yaml`

---
