# Architecture (MVP)

**FE (React+Vite) →** HTTP/JSON → **BE (Spring Boot 3, Java 21) →** JDBC → **Postgres (Neon)**

- Auth: stub (session/JWT later)
- Core entities: users, stories, trust_events
- Migrations: Flyway (V1 applied)
- Envs: DB_URL only (OpenAI later)