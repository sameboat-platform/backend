![Backend CI](https://github.com/ArchILLtect/sameboat-backend/actions/workflows/backend-ci.yml/badge.svg)
# SameBoat Backend (Spring Boot + Java 21)

## Run locally
1. Copy `.env.example` → `.env` and fill `DB_URL` (Neon).
2. Or set `DB_URL` in your Run Configuration env vars.
3. Start:
   ```bash
   DB_URL=... ./mvnw spring-boot:run
    ```
4. Health check: GET /health → ok

## Migrations
Flyway runs automatically on startup from src/main/resources/db/migration.

---