# SameBoat Backend Instructions

Covers local development, environment setup, database migrations, auth stub usage, testing (including optional migration schema verification), and troubleshooting.

---
## 1. Tech Stack (Backend)
- Java 21 / Spring Boot 3.5
- Postgres (Neon cloud or local Docker)
- Flyway for schema migrations (immutable: never edit applied migration files)
- Testcontainers (optional locally; used for schema verification test)

---
## 2. Environment Variables
Priority order now (as of datasource config update):
1. `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
2. Alias set (`DB_URL`, `DB_USER`, `DB_PASSWORD`) – only used if the Spring-specific vars are absent
3. Local fallback: `jdbc:postgresql://localhost:5432/sameboat` with user/password `postgres/postgres`

| Variable | Purpose | Example |
|----------|---------|---------|
| SPRING_DATASOURCE_URL | Primary JDBC URL | `jdbc:postgresql://localhost:5432/sameboat` |
| SPRING_DATASOURCE_USERNAME | DB user | `postgres` |
| SPRING_DATASOURCE_PASSWORD | DB password | `postgres` |
| DB_URL (alias) | Secondary URL fallback | `jdbc:postgresql://localhost:5432/sameboat` |
| DB_USER (alias) | Secondary user fallback | `postgres` |
| DB_PASSWORD (alias) | Secondary password fallback | `postgres` |
| SPRING_PROFILES_ACTIVE | Spring profile | `dev` |
| SKIP_MIGRATION_TEST | Skip migration container test | `1` |

When using Neon:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>:5432/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
```

> Do NOT use the non-JDBC scheme (`postgresql://user:pass@host/db`). Always start with `jdbc:postgresql://`.


---
## 3. Running Locally
### Option A: Neon (Hosted Postgres)
```cmd
set SPRING_PROFILES_ACTIVE=dev
set SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>:5432/<db>?sslmode=require
set SPRING_DATASOURCE_USERNAME=<user>
set SPRING_DATASOURCE_PASSWORD=<password>
./mvnw spring-boot:run
```

### Option B: Local Docker Postgres
```cmd
docker run --name sameboat-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=sameboat -p 5432:5432 -d postgres:16
set SPRING_PROFILES_ACTIVE=dev
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sameboat
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
./mvnw spring-boot:run
```

### Option C: Convenience Script
Use the provided `run-dev.cmd` (auto-falls back to local defaults if nothing set):
```cmd
run-dev.cmd
```

Health check:
```
GET http://localhost:8080/health         -> {"status":"ok"}
GET http://localhost:8080/actuator/health -> Spring actuator info
```

---
## 4. Auth Stub (Week 2 Mode)
- Endpoint: `POST /auth/login` with JSON `{ "email": "dev@local", "password": "dev" }`
- On success: returns user object and sets cookie `SBSESSION=<opaque-token>` (HttpOnly; SameSite=Lax)
- Use cookie automatically for `GET /me`, `PATCH /me`, `POST /auth/logout`

Example login request:
```http
POST /auth/login
Content-Type: application/json

{"email":"dev@local","password":"dev"}
```

Logout:
```http
POST /auth/logout
```

Example authenticated fetch:
```http
GET /me
Cookie: SBSESSION=<token>
```

---
## 5. Database Migrations (Flyway)
Location: `src/main/resources/db/migration`

Applied versions (current):
- `V1__init.sql`
- `V2__users_and_sessions.sql`
- `V3__users_additional_columns.sql`

### Immutability Policy
Once a migration hits `main`, DO NOT edit it. Create a new `V{n+1}__*.sql` for changes.

### On Checksum Mismatch
```
Migration checksum mismatch for migration version 2
```
Preferred:
1. Revert accidental edit.
2. Or add a corrective new migration.

Only if schema truly matches the modified file:
```cmd
./mvnw -Dflyway.url=%SPRING_DATASOURCE_URL% -Dflyway.user=%SPRING_DATASOURCE_USERNAME% -Dflyway.password=%SPRING_DATASOURCE_PASSWORD% flyway:repair
```

### Validating Migrations
```cmd
./mvnw -Dflyway.url=%SPRING_DATASOURCE_URL% -Dflyway.user=%SPRING_DATASOURCE_USERNAME% -Dflyway.password=%SPRING_DATASOURCE_PASSWORD% flyway:info
```

---
## 6. Migration Schema Verification Test (Optional)
Skipped by default via system property `skip.migration.test=true`.

Run explicitly (Docker required):
```cmd
mvn -Dskip.migration.test=false test
```

---
## 7. Testing
Standard run:
```cmd
mvn test
```
Include migration verification:
```cmd
mvn -Pwith-migration-test test
```

---
## 8. Common Troubleshooting
| Issue | Cause | Resolution |
|-------|-------|------------|
| `Unable to connect to the database` | Missing `-Dflyway.*` when using plugin directly | Add `-Dflyway.url/user/password` |
| Checksum mismatch | Edited applied migration | Revert or add new migration; repair only if correct |
| 401 login | Stub password wrong | Use `dev` |
| Cookie missing | Fetch without credentials include | Add proper fetch config |
| Migration test aborts | Docker off / skip flag | Start Docker or remove skip |
| URL warning at startup | Non-JDBC URL or alias mismatch | Use `jdbc:postgresql://...` |

---
## 9. Adding a New Migration
1. `src/main/resources/db/migration/V{next}__name.sql`
2. Prefer idempotent DDL
3. (Optional) Run migration test
4. Commit & PR

---
## 10. Future (Week 3+ Preview)
- Password hashing & registration
- JWT or improved session rotation
- Audit logging / session cleanup

---
## 11. Quick Command Cheat Sheet
```cmd
:: Run backend (Neon example)
set SPRING_PROFILES_ACTIVE=dev
set SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>:5432/sameboat?sslmode=require
set SPRING_DATASOURCE_USERNAME=sameboat_app
set SPRING_DATASOURCE_PASSWORD=***
./mvnw spring-boot:run

:: Run backend (local default via script)
run-dev.cmd

:: Tests (skip migration test)
mvn test

:: Tests (include migration schema verification)
mvn -Pwith-migration-test test

:: Flyway info (direct)
./mvnw -Dflyway.url=%SPRING_DATASOURCE_URL% -Dflyway.user=%SPRING_DATASOURCE_USERNAME% -Dflyway.password=%SPRING_DATASOURCE_PASSWORD% flyway:info
```

---
## 12. Datasource Startup Logging
A startup component logs (sanitized) datasource info:
- `Datasource URL: jdbc:postgresql://***@host/db?...`
- Warns if URL does not start with `jdbc:postgresql://`
- Logs `Datasource user: <username>`
Use this to quickly confirm which source (Spring vs alias vs fallback) was selected.

---
## 13. Immutability Tooling (Applied Migrations)
To prevent modification of already-applied Flyway migration files (V1+, historical versions), use the provided scripts.

| Script | Platform | Example |
|--------|----------|---------|
| `scripts/check-migration-immutability.sh` | Bash / CI | `scripts/check-migration-immutability.sh origin/main` |
| `scripts/check-migration-immutability.ps1` | PowerShell | `pwsh scripts/check-migration-immutability.ps1 -BaseRef origin/main` |
| `scripts/check-migration-immutability.cmd` | Windows CMD | `scripts\check-migration-immutability.cmd` |

Behavior:
- Allows newly added `V#__*.sql` files (status A)
- Fails if an existing `V#__*.sql` is modified, deleted, renamed, or copied
- Safe fallback (PASS) if the base ref cannot be resolved (e.g., shallow clone); CI fetches full history so this should not occur there

### Pre-commit Hook Setup (Optional)
```bash
git config core.hooksPath .githooks
```
The provided `.githooks/pre-commit` runs the Bash script and blocks the commit if a historical migration was altered.

### CI Integration
GitHub Actions workflow executes the Bash script before running tests. The build fails on violation.

---
## 14. Conventions
- No logging of session tokens or raw credentials.
- Every new endpoint ships with at least one test.
- Migrations immutable after merge.

---
## 15. Support / FAQ
If migration chain fails locally:
1. Pull latest `main`
2. Drop & recreate local DB if safe
3. Re-run app
4. Run `flyway:info` with explicit credentials for diagnostics

---
_Updated after datasource priority & logging enhancements and migration immutability tooling (Week 2 refinement)._
