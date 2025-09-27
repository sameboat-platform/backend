# Week 2 Plan (Revised) — SameBoat Honors Project (Fall 2025)

This revised plan narrows scope to the highest‑leverage vertical slice (auth/session + current user profile) while adding explicit acceptance criteria, sequencing, guardrails against migration drift, and clearer Definition of Done.

---
## 1. Week 2 Intent / Outcome
Deliver a reliable authenticated user core: login stub → session token → `/me` fetch → profile update. Establish automation (tests + CI + migration immutability) so future auth hardening (Week 3) is low risk.

---
## 2. Scope (In / Out)
**In (Committed):**
- Flyway V2 finalized (users + sessions) + migration immutability rules
- Backend: `/auth/login`, `/auth/logout`, `GET /me`, `PATCH /me`
- Opaque session token (cookie `SBSESSION`) validation per request
- Minimal user profile fields (email, displayName, avatarUrl, bio, timezone, role)
- Purge or ignore expired sessions at lookup time (simple check; no scheduled job)
- Backend tests: unit + MockMvc integration (with Testcontainers Postgres)
- Frontend: Login form + display basic user info (read‑only after PATCH tested via API client) – edit UI optional (see stretch)
- CI: Postgres service, coverage gate (≥ 70% backend; frontend placeholder gate if tests present)
- Docs: updated run instructions, API contract, milestone journal skeleton

**Deferred (Explicitly Out):**
- JWT issuance/verification
- Registration flow / password hashing
- Profile picture upload handling / storage
- PATCH profile rich validation (only basic field presence/length)
- Admin endpoints / user listing
- Session cleanup job / rotation strategy
- Frontend profile edit form (moved to top of Week 3 unless time remains)

**Stretch (Only if Slack Appears Late Thu/Fri):**
- Frontend PATCH profile form
- Expired session purge command/test
- Simple audit log (login, logout)

---
## 3. Constraints & Assumptions
- Local + CI Postgres reachable; Testcontainers used for integration tests.
- Migration V1 already applied (baseline). V2 must NOT be edited after first commit to main; new changes require V3+.
- Dev auth rule: any email + password "dev" → success.
- Security minimal: CSRF disabled for now; cookie SameSite=Lax, HttpOnly.
- Acceptable for all users to default role USER.

---
## 4. Sequenced Workstream (Vertical Slice Ordering)
1. Database & Migration hardening (V2 freeze + test)
2. Entities/Repositories (User, Session) + basic service layer
3. Session token issuance + lookup (AuthService)
4. `/auth/login` + issue cookie; integration test
5. `/me` (GET) via cookie; integration test
6. `/auth/logout` + invalidation; integration test
7. `PATCH /me` (service + persistence) + integration test
8. Frontend: login form + fetch `/me` display
9. CI enhancements (Postgres service + coverage thresholds)
10. Docs & journal updates; risk review

Stop early if any stage unstable; do not start frontend until backend slice (steps 1–7) green locally & in PR CI.

---
## 5. Detailed Tasks
### 5.1 Database & Flyway
- Confirm V2 file contents match schema needs (users + sessions).
- Add a migration test: start empty DB, run migrations, assert tables + key constraints.
- Add README/`instructions.md` note: "Never modify applied migrations; create new V# files." 
- (Optional late stretch) Add a pre-commit or CI check: `git diff --name-only origin/main...HEAD` fails if applied migration file changed.

### 5.2 Domain Model
UserEntity fields: id (UUID), email (unique), displayName, avatarUrl, bio, timezone, role, createdAt, updatedAt.
SessionEntity: id (UUID), userId FK, sessionToken (unique), createdAt, expiresAt.

### 5.3 Auth & Sessions
- AuthService: login(email,password) -> (user, sessionToken)
- Validate password == "dev" (constant for now); otherwise 401.
- Generate opaque token (UUID or secure random 32 bytes base64).
- Expiry: now + 7 days.
- Lookup filter: extract cookie, load session + user, ensure not expired.

### 5.4 SecurityConfig
- Permit `/actuator/health`, `/auth/login`.
- Require auth for `/auth/logout`, `/me`.
- Stateless; custom filter populates SecurityContext with AuthPrincipal.

### 5.5 Controllers
- POST `/auth/login` => Set-Cookie SBSESSION=token; return `LoginResponse`.
- POST `/auth/logout` => delete session; clear cookie (Set-Cookie with expired);
- GET `/me` => current user (UserDto) or 401.
- PATCH `/me` => partial update (nullable fields). Reject empty body (400) and overly long fields (> 512 chars bio, > 100 displayName) minimal validation.

### 5.6 Observability (Minimal)
- Log INFO on login (user id), WARN on invalid/expired session usage.
- No PII beyond email in logs.

### 5.7 Testing Strategy
Unit:
- UserService update behavior (ignores nulls, trims strings?)
- AuthService token creation sets 7-day expiry
- Expired session rejected

Integration (Testcontainers):
- Login happy → 200 + Set-Cookie
- Login wrong password → 401
- `/me` without cookie → 401
- `/me` with cookie → 200
- PATCH `/me` updates persisted fields
- Logout then `/me` → 401
- Expired session scenario (manually set expires_at past) → 401

Migration Test:
- After Flyway migrate, query information_schema or `pg_catalog` to assert `users` + `sessions` tables exist, with unique index on email & session_token.

Frontend:
- AuthProvider: login updates state; logout clears state
- LoginPage: form submit calls endpoint (MSW)
- Protected route redirect on 401

### 5.8 CI Enhancements
- Add Postgres service (or rely on Testcontainers only; ensure container pulls don't exceed timeout).
- Coverage thresholds: backend (Jacoco) 70%, frontend 0 or 70% if tests ready by Thu; failing gate disabled only with rationale in PR.
- Separate job stage: flyway validate (fast failure) before test stage.

### 5.9 Docs
Update or create:
- `instructions.md`: run backend with local Postgres, how to login stub, migration policy
- `docs/api.md`: endpoints + samples + error envelope format
- Journal skeleton: daily bullet placeholders
- Add `RISKS.md` (or section) listing current risks (see Section 9)

---
## 6. Acceptance Criteria
Backend:
- Hitting `/auth/login` with valid dev creds sets HttpOnly cookie; cookie reused successfully on `/me`.
- Expired session (manually adjusted) returns 401 with JSON error `{"error":"SESSION_EXPIRED"}`.
- PATCH `/me` updates only provided fields, returns updated user.
- Attempt to update bio > 512 chars → 400.
- Migration test passes against clean database.

Frontend:
- Submitting login form (email + dev) shows user email on a simple dashboard/profile component.
- 401 from `/me` triggers redirect to `/login`.

Quality:
- Coverage ≥ thresholds; no mutation of V2 after initial merge.
- All new endpoints have at least one integration test.

---
## 7. Definition of Done (Consolidated)
- All acceptance criteria satisfied.
- CI pipeline green (validate, test, coverage) on main.
- No TODO or FIXME in auth/session code (unless tracked in `RISKS.md` with owner + date).
- Docs (instructions + API + journal) updated and committed.
- Flyway `flyway:validate` clean (no pending / mismatched checksums).

---
## 8. Risks & Mitigations
| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Migration file edited post-apply | Prod drift later | Medium | Immutability policy + review + optional CI check |
| Over-scoping frontend edit UI | Delays backend stability | High | Defer profile edit UI (stretch) |
| Session token leakage via logs | Security concern | Low | Log only user id + not token |
| Testcontainers startup flake | Slower feedback | Medium | Increase readiness timeout; run flyway validate first |
| Expired sessions pile up | Table bloat | Low (week 2) | Add cleanup Week 3 |

---
## 9. Daily Execution Plan (Target Cadence)
- Mon: Finalize V2 + migration test; entities/repos; AuthService skeleton.
- Tue: Login + session issuance + filter + integration tests.
- Wed: `/me` + PATCH + validation + error envelope + tests.
- Thu: Frontend login + `/me` consumption + MSW tests; CI coverage gate wiring.
- Fri: Logout flow, expired session test, docs/journal, risk review, polish.

---
## 10. Metrics / Targets
- Backend line coverage ≥ 70%; critical packages (auth, user, session) ≥ 80% branch if time allows.
- Mean local test run < 25s (optimize heavy container reuse if slower).
- Zero flaky test retries in final run.

---
## 11. Guardrails & Policies
- Do not modify existing applied migration scripts; create new versions for changes.
- Every new endpoint accompanied by at least one integration test in same PR.
- No committing secrets (only dev placeholder passwords allowed).
- Error responses standardized: `{ "error": UPPER_SNAKE_CODE, "message": humanReadable }`.

---
## 12. Stretch Goals (End of Week Only)
- Frontend profile edit form + optimistic update
- Session purge CLI or scheduled task (only if core stable)
- Simple audit log table (event_type, user_id, created_at)

---
## 13. Week 3 Preview (Preparation Targets)
- Introduce password hashing + minimal registration endpoint
- Consider migrating sessions → JWT access + refresh token pair
- Add rate limiting / brute force guard (basic in-memory) for login
- Add audit logging & structured logging improvements

---
## 14. Quick Run Cheat Sheet (Local)
```bash
# (Example) Start Postgres (Docker) if not running
# docker run --name sameboat-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=sameboat -p 5432:5432 -d postgres:16

SET SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
# Login: POST /auth/login {"email":"dev@local","password":"dev"}
# Use returned cookie for /me and PATCH /me
```

---
## 15. Status Tracking Template (Fill During Week)
| Day | Completed | Blockers | Adjustments |
|-----|-----------|----------|-------------|
| Mon |           |          |             |
| Tue |           |          |             |
| Wed |           |          |             |
| Thu |           |          |             |
| Fri |           |          |             |

---
**Summary:** Reduced scope concentrates on robust backend auth/profile core with essential automation. Frontend kept intentionally minimal to conserve cognitive budget for Week 3 security hardening. Guardrails (migration immutability, acceptance criteria, error envelope) aim to minimize refactor churn.

