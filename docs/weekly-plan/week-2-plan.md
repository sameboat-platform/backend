# Week 2 Plan — SameBoat Honors Project (Fall 2025)

> NOTE (Revised): This original Week 2 plan has been superseded by `week_2_plan_same_boat_honors_project_fall_2025_revised.md` which narrows scope, adds acceptance criteria, and introduces a migration immutability policy plus a schema verification test. Keep this file for historical intent; consult the revised plan for execution details.

**Context loaded from Week 1:** Vite + React + TS frontend, Spring Boot (Java 21) backend, Neon Postgres + Flyway, CI (GitHub Actions) green for both repos, healthcheck wired (status: **UP**), initial smoke + health tests passing, docs skeleton + milestone reflections started.

---

## Week 2 Objectives (High‑Level)

1) **Auth stubs** suitable for dev/test now, with a clean path to JWT later.
2) **User profiles**: minimal schema, CRUD-lite endpoints (self‑service only in Week 2).
3) **Test coverage**: backend (MockMvc + Testcontainers) and frontend (Vitest + React Testing Library + MSW) beyond smoke.
4) **Docs + reflections**: update `instructions.md`, API docs, and write **Milestone 2** journal + reflection.

**Non‑Goals (deferred):** third‑party login (OAuth), production‑grade JWT, account recovery, admin UX, full profile settings UI.

---

## Week 2 IMPLEMENTATION OUTCOME (Added Post‑Execution)

### What Actually Got Built
- Replaced the simple dev credential stub with **BCrypt password hashing** (`PasswordEncoder` bean) and a **real registration flow** (`POST /auth/register`).
- Introduced distinct 401/409 error codes: `UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`, plus `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR`.
- Added session alias cookie support (`SBSESSION` primary, `sb_session` accepted) and environment‑aware cookie attributes (prod: Secure + domain `.sameboat.<tld>`; dev/test: no Secure/domain).
- Implemented **type‑safe configuration binding** via `SameboatProperties` (auth, cookie, session, cors) with Spring configuration processor metadata.
- Added `application-prod.yml` with profile activation, secure cookie, extended 14‑day TTL, and restricted CORS origins.
- Centralized CORS configuration reading from `sameboat.cors.allowed-origins` list; credentials enabled for cookie auth.
- Added registration & login integration tests: happy path, duplicate email, wrong password, garbage cookie (UNAUTHENTICATED), expired session (SESSION_EXPIRED), alias cookie test.
- JaCoCo **coverage gate set to 70%** instructions (raised from earlier draft 60%). All tests green under `mvn verify`.
- Documentation fully updated (`README.md`, `docs/api.md`) to reflect new flows & error taxonomy.

### Deviations from Original Plan
| Original Plan Item | Actual Implementation / Change |
|--------------------|--------------------------------|
| Dev header auth or simple stub only | Upgraded to hashed password + registration sooner (Week 2 instead of Week 3) |
| Session token conceptually opaque (maybe in-memory) | Persisted Postgres sessions with expiry + touch logic (no in‑memory fallback) |
| Generic `UNAUTHORIZED` error code | Replaced with clearer `UNAUTHENTICATED` / `BAD_CREDENTIALS` / `SESSION_EXPIRED` distinctions |
| Auto find-or-create user on login | Deprecated & removed in favor of explicit registration (except optional dev auto-create flag) |
| Coverage target 60% | Adopted 70% earlier to enforce discipline |
| Planned DevAuthFilter | Not needed; session filter + registration/login replaced it |

### Deferred / Not Done (Still Future Work)
- Password reset / email verification
- JWT or token rotation strategy (still planned for later milestone)
- Idle timeout & session rotation / refresh semantics
- Role-based authorization beyond basic `ROLE_USER`
- OpenAPI spec sync with generated endpoints
- Frontend MSW + coverage improvements (not fully documented here)

### Current Error Codes
`UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`, `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR`.

### Key Risks / Follow-Up
| Risk | Mitigation / Next Step |
|------|------------------------|
| Session table growth | Add scheduled cleanup (Week 3) for expired sessions |
| Lack of password complexity checks | Add validation annotations + custom constraint next iteration |
| No rate limiting on login | Introduce minimal attempt counter or bucket4j filter |
| JWT migration path not yet prototyped | Spike in Week 3: adapt AuthPrincipal from session to token claims |

---

## Architecture Notes for Week 2

- **Security strategy (Week 2):**
  - Active now: BCrypt + server-side sessions + cookie auth across dev/test/prod.
  - Dev/test may auto-create users if `sameboat.auth.dev-auto-create=true` with stub password (defaults false in dev, true only in test profile when set).
  - Prod adds Secure cookie & domain; all non-health endpoints require authentication.

- **Session model:** Postgres-persisted sessions with `expiresAt` and `lastSeenAt` touch. Filter checks expiry = `SESSION_EXPIRED`; invalid UUID or missing = `UNAUTHENTICATED`.

- **Forward path to JWT:** abstraction preserved (AuthPrincipal). Can add a parallel token issuance service without refactoring controllers significantly.

> Revised outcome note: The earlier notion of a DevAuthFilter was superseded by the combined registration + session approach.

---

## Backend Tasks (Spring Boot)

### 1) Database (Flyway)

**Migration: `V2__users_and_sessions.sql` (conceptual design)**
```sql
-- users
create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  email text not null unique,
  display_name text,
  role text not null default 'USER',
  avatar_url text,
  bio text,
  timezone text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_users_email on users (email);

-- sessions (simple server-side session stub)
create table if not exists sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  session_token text not null unique,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null
);
create index if not exists idx_sessions_token on sessions (session_token);
```

> Current repo state: `V1__init.sql`, `V2__users_and_sessions.sql`, `V3__users_additional_columns.sql` (immutability enforced).

### 2) Domain + DTOs

```text
com.sameboat.auth
  AuthController
  AuthService
  DevAuthFilter (profile: dev,test)
  SecurityConfig
  AuthPrincipal (record)

com.sameboat.user
  UserEntity
  UserRepository
  UserService
  UserController
  dto/
    UserDto (public view)
    UpdateUserRequest
    CreateUserRequest (optional, may defer open registration)

com.sameboat.session
  SessionEntity
  SessionRepository
```

**DTOs (minimal):**
```java
public record UserDto(UUID id, String email, String displayName, String avatarUrl, String bio, String timezone, String role) {}
public record UpdateUserRequest(String displayName, String avatarUrl, String bio, String timezone) {}
public record LoginRequest(String email, String password) {}
public record LoginResponse(UserDto user) {}
```

### 3) Security Config (Final Week 2 State)
- Public: `/actuator/health`, `/auth/login`, `/auth/register` (and `/api/auth/*` variants).
- Authenticated: `/auth/logout`, `/me` (GET/PATCH) + any future protected endpoints.
- Cookie names accepted: `SBSESSION` (primary), `sb_session` (alias).
- Distinct 401 vs 409 handling via mapped error codes.

### 4) Controllers (Implemented)
- `POST /auth/register` (returns `{userId}` + cookie) — 200, 409 on duplicate.
- `POST /auth/login` — returns user envelope or `BAD_CREDENTIALS`.
- `POST /auth/logout` — invalidates session & clears cookie.
- `GET /me` — returns user or `UNAUTHENTICATED` / `SESSION_EXPIRED`.
- `PATCH /me` — partial update with validation; empty body → `VALIDATION_ERROR`.

### 5) Tests (Backend)
Implemented suite includes:
- Login success/failure
- Registration success + duplicate + wrong password + correct follow-up login
- Session expiration (primary + alias cookie)
- Garbage / missing cookie (UNAUTHENTICATED)
- Profile patch success & validations

### 6) CI Enhancements (Backend)
- JaCoCo gate **70%** (passed)
- Migration immutability check script integrated
- Optional migration schema profile remains (`-Pwith-migration-test`)

---

## Deliverables Checklist (Week 2) — Updated Status

**Backend**
- [x] Migrations V1–V3 applied (immutability enforced)
- [x] Entities/Repos/Services for User + Session
- [x] SecurityConfig + session filter (DevAuthFilter superseded)
- [x] Controllers: `/auth/register`, `/auth/login`, `/auth/logout`, `/me` (GET/PATCH)
- [x] Registration flow (email uniqueness + BCrypt hashing)
- [x] Distinct auth error codes implemented (`UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`)
- [x] Cookie policy (dev vs prod: Secure/domain & TTL) + alias cookie `sb_session`
- [x] Type-safe configuration (`SameboatProperties`) + prod profile
- [x] Unit + integration tests (login, register, duplicate, wrong password, expired, alias cookie, profile patch)
- [x] Migration schema test profile (`with-migration-test`)
- [x] Coverage gate (70%) configured & passing

**Frontend** *(separate repo progress – snapshot)*
- [ ] AuthProvider + API client (cookie path) *(partial / in progress)*
- [ ] Login + Profile pages *(baseline login UI present; profile edit in progress)*
- [ ] ProtectedRoute *(pending)*
- [ ] MSW tests *(initial stubs only)*
- [ ] Coverage gate *(deferred – rationale documented)*

**Docs**
- [x] Updated `instructions.md` / README with auth + config + prod profile
- [x] Updated `docs/api.md` (new endpoints + error codes & registration)
- [x] Week 2 journal & reflection drafted
- [x] This plan updated with outcome + deviations

**Additional (not in original checklist)**
- [x] Application prod profile YAML (`application-prod.yml`)
- [x] Session alias cookie support
- [x] Enhanced CORS configuration (list binding + credentials)

---
## Milestone 2 Deviation Changelog (Summary Lines)
For reflection / retrospective use:
- Introduced full BCrypt hashing + explicit registration one week earlier than planned (replaced simple dev stub).
- Adopted persistent Postgres-backed sessions only (dropped tentative in-memory fallback) to simplify state consistency.
- Replaced single `UNAUTHORIZED` response with granular auth error taxonomy (`UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`).
- Removed implicit find-or-create login path; enforced explicit registration (retained dev auto-create flag only for test convenience).
- Raised coverage requirement from 60% → 70% mid-week to enforce higher quality earlier.
- Eliminated planned DevAuthFilter in favor of a unified `SessionAuthenticationFilter` + configuration properties model.
- Added prod profile early (secure cookie, domain, extended TTL) to reduce later environment drift risk.
- Added type-safe `SameboatProperties` binding to centralize config and eliminate scattered `@Value` usage.
- Implemented alias cookie name (`sb_session`) for forward compatibility / client naming flexibility.

---

## Definition of Done (Updated)
- Registration + login + logout + /me (GET/PATCH) fully functional with hashed passwords.
- Session expiration + error code differentiation validated via tests.
- Coverage ≥ 70% (gate green).
- Migration immutability enforced; no historical rewrites.
- Documentation (README + API + plan) reflects actual implementation.
- Configurable via environment (sameboat.* properties) with prod overrides.

---

## Week 3 Preview / Action Items
| Item | Goal |
|------|------|
| Add password complexity validation | Strengthen credential policy |
| Session pruning job | Prevent stale row buildup |
| Evaluate JWT vs extended session model | Decide auth token trajectory |
| Add rate limiting to login endpoint | Throttle brute force |
| OpenAPI integration | Sync live endpoints into spec |
| Frontend test & coverage catch-up | Align with backend quality gates |

---
