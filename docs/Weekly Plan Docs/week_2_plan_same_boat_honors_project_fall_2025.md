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

## Architecture Notes for Week 2

- **Security strategy (Week 2):**
  - Use Spring profiles: `dev`, `test`, `prod`.
  - In `dev`/`test`: enable **Dev Header Auth** (accepts `X-Dev-User` email) **or** simple credential stub (email + password `dev`), returning a **server session cookie** (HttpOnly). Keep it minimal and replaceable.
  - In `prod`: only the health endpoints are public; all others require auth (will be refined Week 3+). JWT remains a planned migration target.

- **Session model:** ephemeral server‑side sessions stored in Postgres (`sessions` table) or in‑memory map under `dev`/`test` profile. Prefer Postgres to make e2e predictable in CI.

- **Forward path to JWT:** isolate auth in `/auth/**` controller + `AuthService`; add an `AuthPrincipal` and a `JwtService` interface with a no‑op dev impl. When ready, swap impl.

> Revised plan adjustment: final implementation sequence focuses first on stable migrations (V1–V3), then `login` + `/me`, deferring optional profile edit UI to a stretch goal.

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

> Current repo state: `V1__init.sql`, `V2__users_and_sessions.sql` (as designed above) and a reconciliation `V3__users_additional_columns.sql` to align early V1 schema with the richer user model *without editing historical files* (immutability rule).

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

### 3) Security Config (Week 2 stub)

- Public: `GET /actuator/health`, `POST /auth/login`.
- Authenticated: `POST /auth/logout`, `GET /me`, `PATCH /me`.
- Cookie: `SBSESSION=<opaque token>` `HttpOnly; SameSite=Lax; Secure` (Secure on non‑dev).

```java
// Example SecurityFilterChain bean (simplified excerpt)
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/auth/login").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(devAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
}
```
> Revised plan clarifies naming: although termed "session", behavior remains stateless per request validation against stored session entries.

### 4) Controllers (Week 2)

**AuthController**
- `POST /auth/login` → accept `LoginRequest`.
  - **DEV rule:** if password equals `"dev"` create or find user by email, issue session token valid 7 days; return `LoginResponse` (with `Set-Cookie: SBSESSION=…`).
  - `test` profile can behave the same.
- `POST /auth/logout` → invalidate session token (delete row) + clear cookie.

**UserController**
- `GET /me` → return current user `UserDto`.
- `PATCH /me` → update subset of fields from `UpdateUserRequest`.

### 5) Tests (Backend)

- **Unit:** `UserServiceTest`, `AuthServiceTest` covering create/find, update, session creation/expiry logic (e.g., 7 days, expired purges).
- **Integration (MockMvc + Testcontainers):**
  - `POST /auth/login` happy path + invalid password.
  - Cookie propagation to `GET /me`.
  - `PATCH /me` persists changes; attempt without cookie → 401.
  - `POST /auth/logout` → subsequent `GET /me` 401.

> (Optional) nightly job to prune expired sessions (Deferred — not required for Week 2 DoD).

### 6) CI Enhancements (Backend)

- Use **Testcontainers** (no dedicated Postgres service container required) for integration tests & migration validation.
- Cache Maven dependencies for faster builds.
- Add coverage gate (target ≥ 70%).
- Separate migration schema test via Maven profile `with-migration-test` (runs Testcontainers migration check).

**Example commands:**
```bash
mvn verify                   # regular tests (migration test skipped)
mvn -Pwith-migration-test test  # includes migration schema verification
```

---

## Frontend Tasks (Vite + React + TS)

### 1) API Client + Auth Context

- Create `/src/lib/api.ts` with a `fetchJson` helper that includes credentials (cookie), throws on non‑2xx, and handles 401.
- Add `/src/context/AuthProvider.tsx` exposing `{ user, login(email, password), logout(), refreshMe() }`.
- Consider React Query for caching/invalidations.

### 2) Screens + Routes

- `LoginPage.tsx` → email + password (placeholder; password default text: "dev").
- `ProfilePage.tsx` → view + (optional) edit; PATCH optional if time.
- `ProtectedRoute` wrapper redirects unauthenticated users to `/login`.

### 3) Types + API contracts

```ts
export type UserDto = {
  id: string;
  email: string;
  displayName?: string;
  avatarUrl?: string;
  bio?: string;
  timezone?: string;
  role: 'USER' | 'ADMIN';
};

export type LoginRequest = { email: string; password: string };
export type LoginResponse = { user: UserDto };
```

### 4) Tests (Frontend)

- **Unit:** components render, form validation, API helpers.
- **Integration (MSW):**
  - successful login → sets user context, navigates to `/profile`.
  - bad login → shows error.
  - `PATCH /me` updates state (if implemented this week).
  - 401 interceptor redirects to `/login`.

### 5) CI (Frontend)

- Vitest + jsdom; add coverage gate (≥ 70%) if time, else document deferral.

---

## Deliverables Checklist (Week 2)

**Backend**
- [ ] Migrations V1 through V3 applied (V3 reconciliation) — no edits to historical files
- [ ] Entities/Repos/Services for `User` + `Session`
- [ ] SecurityConfig + DevAuthFilter (profiles: dev,test)
- [ ] Controllers: `/auth/login`, `/auth/logout`, `/me` (GET/PATCH)
- [ ] Unit + integration tests (MockMvc + Testcontainers)
- [ ] Migration schema test profile (with-migration-test)
- [ ] CI coverage gate configured

**Frontend**
- [ ] AuthProvider + API client
- [ ] Login + Profile pages (basic UX)
- [ ] ProtectedRoute
- [ ] MSW test suite
- [ ] Coverage gate (or rationale for deferral)

**Docs**
- [ ] Update `instructions.md` (env vars: SPRING_DATASOURCE_URL/USERNAME/PASSWORD or DB_URL alias)
- [ ] Update `docs/api.md` (Week 2 endpoints + example requests/responses)
- [ ] Update milestone journal & reflection

---

## Docs — Proposed Content

### `instructions.md` (additions)

- **Env vars (backend):** `SPRING_PROFILES_ACTIVE=dev`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (or alias `DB_URL`, `DB_USER`, `DB_PASSWORD`).
- **Starting dev:**
  - Backend: `./mvnw spring-boot:run`
  - Frontend: `pnpm dev`
- **Auth (dev stub):**
  - Login with any email + password `dev` at `/auth/login`.
  - App sets `SBSESSION` cookie (HttpOnly); browser sends it automatically.
  - Use `GET /me` to verify; update profile via `PATCH /me`.

### `docs/api.md` (new/updated)

```
POST /auth/login
  req: { email, password }
  res: { user }
  200 → Set-Cookie: SBSESSION=...; HttpOnly; SameSite=Lax

POST /auth/logout
  204; clears cookie

GET /me
  res: UserDto

PATCH /me
  req: UpdateUserRequest
  res: UserDto (updated)
```

**Examples**
```http
POST /auth/login
Content-Type: application/json

{"email":"dev@sameboat.local","password":"dev"}
```

```json
{ "user": { "id":"…","email":"dev@sameboat.local","displayName":"Dev User","role":"USER" } }
```

### `docs/milestones/milestone-2.md`

#### Journal (daily bullets)
- **Mon:** Implement Flyway V2 + V3 reconciliation; scaffold entities/repos.
- **Tue:** AuthController login/logout; session cookie end‑to‑end.
- **Wed:** `/me` GET/PATCH + profile fields; MockMvc tests.
- **Thu:** Frontend AuthProvider, Login/Profile pages; MSW tests.
- **Fri:** CI gates + polish; docs finalized; reflection written.

#### Reflection (draft)
**What went well**
- CI stayed green after introducing DB + auth complexity.
- Testcontainers enabled realistic integration tests with minimal flake.
- Frontend auth flow (cookie + 401 redirect) feels predictable.

**What was hard / surprises**
- Session validation filter iterations.
- MSW cookie handling nuance.

**Key decisions**
- Opaque server session token for Week 2.
- Preserve clean abstraction for JWT migration.

**Next week (Week 3 preview)**
- Password hashing & minimal registration.
- Token strategy exploration (JWT vs refined sessions).
- Audit logging for auth events.

---

## End‑of‑Week Update Template (Fill‑In)

- **Deviations from plan:** …
- **Bugs found/fixed:** …
- **Coverage numbers:** backend …%, frontend …%
- **Open risks:** …
- **Demo notes:** routes to click, test accounts, what to show.

---

## Stretch Goals (only if time remains)

- Add `createdAt/updatedAt` auditing via `@EntityListeners`.
- Basic avatar upload stub (client‑side only) with URL field.
- Add `/users/{id}` GET (self or admin; enforce self in Week 2).

---

## Definition of Done (Week 2)

- Auth stub login/logout works locally & in CI.
- `/me` GET/PATCH implemented with persistence.
- Migration immutability respected (no edits to V1–V3).
- Back + Front CI include coverage gates ≥ 70% (or documented deferral).
- Docs reflect current endpoints & setup.
- Milestone 2 journal + reflection drafted and committed.
