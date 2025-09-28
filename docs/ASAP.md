# ASAP Task List

Purpose: High-priority items to unblock momentum immediately at the start of Week 3. Keep this file short-lived (archive after completion into journal or close as a PR).

---
## 1. Finish Week 2 Frontend Tasks
**Goal:** Minimal end‑to‑end auth UX (login -> /me -> logout -> redirect on 401) plus basic profile display.

| Subtask | Status | Priority | Notes / Next Action |
|---------|--------|----------|----------------------|
| Implement login form (email + password) | ☐ | P0 | POST /auth/login; show BAD_CREDENTIALS message |
| Persist session via cookie automatically | ☐ | P0 | Browser handles; ensure fetch uses `credentials: 'include'` |
| Global fetch wrapper w/ 401 handling | ☐ | P0 | Redirect to /login on UNAUTHENTICATED or SESSION_EXPIRED |
| /me fetch on app mount (AuthProvider) | ☐ | P0 | Set user context; null if 401 |
| Display user email/avatar placeholder | ☐ | P1 | Simple component (ProfileBadge) |
| Logout button (POST /auth/logout) | ☐ | P1 | Clear UI state after 204 |
| Patch profile (optional this sprint) | ☐ | P2 | Only if time; update displayName & bio |
| Add MSW mocks for auth endpoints | ☐ | P1 | Mirror backend codes: BAD_CREDENTIALS, UNAUTHENTICATED, SESSION_EXPIRED |
| Vitest tests: login success & 401 redirect | ☐ | P1 | Cover redirect logic + context update |
| Coverage report (baseline) | ☐ | P2 | Document %; gate can come later |

**Definition of Done (Frontend slice):** User can login, see their email, refresh retains session, expired session triggers redirect, logout returns to login screen.

---
## 2. Fix / Enhance Backend CI
**Goal:** Strengthen pipeline fidelity & transparency. Current CI green but missing schema + stronger coverage signal.

| Subtask | Status | Priority | Notes / Next Action |
|---------|--------|----------|----------------------|
| Add Testcontainers Postgres to main `verify` job | ☐ | P0 | Re-run migrations against real PG; adapt memory/timeouts |
| Keep fast H2 job (matrix) for quick feedback | ☐ | P2 | Parallel speed vs fidelity balance |
| Integrate migration immutability script (already) – enforce fail-fast | ☑ | Done | Present; ensure docs reference remains |
| Promote migration profile to default (or separate job) | ☐ | P1 | Possibly `verify -Pwith-migration-test` in second job |
| Publish JaCoCo report artifact | ☐ | P1 | Upload HTML for PR inspection |
| Add coverage XML + PR comment (optional) | ☐ | P2 | Use action (e.g. codecov or custom) |
| Raise coverage gate to 75% after new unit tests | ☐ | P1 | Only after added tests pass locally |
| Add caching for Maven & Testcontainers layers | ☐ | P2 | Optimize build time |
| Add CI badge for coverage (shields.io manual) | ☐ | P3 | After stable coverage baseline |
| Lint / format check (Spotless or Checkstyle) | ☐ | P3 | Future quality gate |

**Definition of Done (CI enhancement):** Main pipeline runs Postgres-backed migration & integration test job + coverage artifact; gate reflects intended minimum.

---
## 3. Add JavaDocs for New / Updated Classes
**Goal:** Provide concise, high-value documentation for maintainability & onboarding.

| Class / File | Status | Priority | Required Doc Elements |
|--------------|--------|----------|-----------------------|
| `AuthController` | ☐ | P0 | Purpose, endpoints summary, error codes mapping |
| `SecurityConfig` | ☐ | P0 | Filter chain ordering, public vs protected endpoints |
| `SessionAuthenticationFilter` | ☐ | P0 | Cookie extraction, expiry handling, context population |
| `SameboatProperties` | ☐ | P0 | Property groups (auth, cookie, session, cors) |
| `UserService` (new methods) | ☐ | P1 | Registration, normalization, hashing rationale |
| `SessionService` (if present) | ☐ | P1 | Touch semantics, expiry strategy |
| `UserController` (updated responses) | ☐ | P1 | Auth codes & validation errors |
| DTOs: `RegisterRequest`, `LoginRequest`, `LoginResponse`, `UpdateUserRequest` | ☐ | P2 | Field intent, validation rules |
| `ExpiredSessionIntegrationTest` | ☐ | P2 | Test purpose & scenario coverage |
| `RegistrationIntegrationTest` | ☐ | P2 | Cases covered (duplicate, wrong password) |
| `BadCookieIntegrationTest` | ☐ | P3 | Distinguish UNAUTHENTICATED vs BAD_CREDENTIALS |
| `CorsConfig` | ☐ | P2 | Origins list binding & credentials rationale |
| `UserEntity` (passwordHash note) | ☐ | P1 | Persistence constraints, lifecycle callbacks |

### JavaDoc Style Guidelines
- Start with a one-line summary sentence.
- Include rationale if behavior differs from standard Spring conventions.
- Reference error codes (`UNAUTHENTICATED`, `BAD_CREDENTIALS`, etc.) where relevant.
- Avoid repeating self-evident field names; focus on intent & constraints.

### Suggested Workflow
1. Add JavaDocs to highest priority classes (controllers, security, filter, properties).
2. Run `mvn -DskipTests javadoc:javadoc` to validate formatting.
3. Follow with service / entity / DTOs.
4. Mark table status as completed (☑) and remove table once all P0/P1 done.

---
## 4. Quick Win Order of Execution
1. Add password complexity + unit test (backend) to unblock frontend expectation of error message structure.
2. Implement frontend login flow & 401 redirect before rate limiting (ensures base path works).
3. Integrate Testcontainers migration test into CI for realism.
4. Add rate limiting + `RATE_LIMITED` error code & test.
5. Add pruning scheduler & test (in-memory clock or manual expiry manipulation).
6. JavaDocs pass P0 classes.
7. Raise coverage gate after new unit tests.

---
## 5. Blocking Issues / Questions (Fill In)
| Issue | Impact | Owner | Resolution Target |
|-------|--------|-------|-------------------|
| (example) Need decision: use Bucket4j vs simple Atomic counters | Could delay rate limit feature | TBD | Day 2 |
| | | | |

---
## 6. Exit Criteria for Closing This File
- All P0 + P1 tasks in sections 1–3 completed and referenced in Week 3 journal.
- CI reflects new coverage gate and Postgres integration.
- JavaDocs generated without warnings for P0 classes.
- File archived (rename to `ASAP-closed-<date>.md`) or removed.

---
*Created: 2025-09-27*

