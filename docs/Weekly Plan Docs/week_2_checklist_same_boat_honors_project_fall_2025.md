# Week 2 Checklist — SameBoat (Fall 2025)

Legend:
- [x] = Completed this week
- [ ] = Not completed (or only partially complete) — notes indicate gaps

> Source Plan: `week_2_plan_same_boat_honors_project_fall_2025_revised.md`

---
## 1. Scope (Committed Items)
| Task | Status | Notes |
|------|--------|-------|
| Flyway V2 finalized (users + sessions) | [x] | `V2__users_and_sessions.sql` present |
| Migration immutability **rules** (policy + enforcement) | [x] | Policy + automated scripts (`scripts/check-migration-immutability.*`) wired into CI |
| `/auth/login` endpoint | [x] | BCrypt + `BAD_CREDENTIALS` error (replaced simple stub) |
| `/auth/register` endpoint (pulled forward) | [x] | Added early; returns `{userId}` + cookie |
| `/auth/logout` endpoint | [x] | Invalidates session + clears cookie |
| `GET /me` endpoint | [x] | Returns `UserDto` or 401 (`UNAUTHENTICATED` / `SESSION_EXPIRED`) |
| `PATCH /me` endpoint | [x] | Partial update; empty body now rejected (`VALIDATION_ERROR`) |
| Opaque session token (SBSESSION cookie) | [x] | Primary name `SBSESSION` (alias `sb_session` accepted) |
| Validate session per request | [x] | `SessionAuthenticationFilter` + expiry check |
| Minimal user profile fields | [x] | email, displayName, avatarUrl, bio, timezone, role |
| Purge OR ignore expired sessions (lookup) | [x] | Ignored if expired (pruning deferred) |
| Backend tests: unit + MockMvc + Testcontainers Postgres | [ ] | Integration extensive; unit minimal; migration profile optional |
| Frontend: login form + display user info | [ ] | Still pending (frontend repo) |
| CI: Postgres service in pipeline | [ ] | Using H2 + optional Testcontainers profile only |
| CI: Coverage gate ≥ 70% | [x] | Gate set & passing (JaCoCo) |
| Docs: run instructions updated | [x] | README + config properties table |
| Docs: API contract (`api.md`) | [x] | Updated with registration & error codes |
| Milestone journal skeleton (Week 2) | [ ] | Not authored yet |

---
## 2. Deferred (Explicitly Out of Scope) — Updated
| Task | Status | Notes |
|------|--------|-------|
| JWT issuance / verification | [ ] | Planned Week 3+ |
| Profile picture upload | [ ] | Deferred |
| Rich validation (beyond basic lengths) | [ ] | Complexity rules pending |
| Admin endpoints / user listing | [ ] | Deferred |
| Session cleanup / rotation strategy | [ ] | Pruning job future |
| Frontend profile edit form | [ ] | Future |
| Password reset / recovery | [ ] | Future |

(Moved to delivered from deferred: Registration & hashing)

---
## 3. Stretch (Not Required) — Attempted?
| Stretch Item | Status | Notes |
|--------------|--------|-------|
| Frontend PATCH profile form | [ ] | Not started |
| Expired session purge command/test | [ ] | Only detection implemented (no purge) |
| Simple audit log (login/logout events) | [x] | Basic INFO/WARN logging in place |

---
## 4. Sequenced Workstream (Plan vs Completion)
| Step | Description | Status | Notes |
|------|-------------|--------|-------|
| 1 | DB & migration hardening | [x] | Immutability scripts + migrations stable |
| 2 | Entities/Repositories | [x] | User + Session (UUID used as token) |
| 3 | Session issuance + lookup service | [x] | Service + filter done |
| 4 | `/auth/login` + test | [x] | Success + failure covered |
| 5 | `/me` GET + test | [x] | Auth + unauth cases |
| 6 | `/auth/logout` + test | [x] | Invalidation verified |
| 7 | `PATCH /me` + test | [x] | Empty body validation added |
| 8 | Frontend login + display | [ ] | Pending |
| 9 | CI enhancements (Postgres, coverage ≥70%) | [~] | Coverage ✓, Postgres service deferred |
| 10 | Docs & journal updates, risk review | [~] | Docs ✓, journal & RISKS file pending |

Legend addition: [~] = Partially complete.

---
## 5. Detailed Tasks Breakdown
### 5.1 Database & Flyway
- [x] Confirm V2 schema content
- [x] Migration test profile (available; optional run) *main pipeline still uses H2*
- [x] README immutability note
- [x] Automated immutability check (scripts + CI)

### 5.2 Domain Model
- [x] `UserEntity` with required fields
- [ ] Separate `sessionToken` column (UUID id reused instead; acceptable simplification)

### 5.3 Auth & Sessions
- [x] Dedicated password hashing (BCrypt)
- [ ] Dedicated `AuthService` abstraction (controller + services currently sufficient)
- [x] Opaque token created
- [x] 7/14-day expiry logic (profile-dependent)
- [x] Filter validates & sets SecurityContext

### 5.4 SecurityConfig
- [x] Public endpoints configured
- [x] Protected endpoints enforced
- [x] Stateless (custom session filter + context set)
- [x] `AuthPrincipal` set

### 5.5 Controllers
- [x] POST `/auth/register`
- [x] POST `/auth/login`
- [x] POST `/auth/logout`
- [x] GET `/me`
- [x] PATCH `/me` (empty body rejected)

### 5.6 Observability
- [x] INFO login/logout event log
- [x] WARN invalid/expired session log

### 5.7 Testing Strategy
- [~] Unit tests (basic `UserServiceTest`; more warranted)
- [x] Integration: login success & failure
- [x] Integration: `/me` auth & no-auth
- [x] Integration: PATCH success
- [x] Integration: PATCH validation failure
- [x] Integration: logout invalidates
- [x] Integration: expired session 401 (`SESSION_EXPIRED`)
- [x] Alias cookie (`sb_session`) expiration test
- [ ] Full migration test enforced in default CI run
- [ ] Testcontainers Postgres for main integration suite

### 5.8 CI Enhancements
- [ ] Postgres service or enforced Testcontainers
- [x] Coverage gate present
- [x] Coverage threshold meets 70% target
- [ ] Separate Flyway validate stage (manual via plugin only)

### 5.9 Docs
- [ ] `instructions.md` (distinct file) *README covers basics*
- [x] `api.md` with endpoint specs
- [ ] Journal (Week 2) skeleton
- [ ] `RISKS.md` extracted (risks summarized inline only)

---
## 6. Acceptance Criteria — Backend
| Criterion | Status | Notes |
|-----------|--------|-------|
| Login sets HttpOnly cookie & usable for `/me` | [x] | Verified in integration tests |
| Expired session 401 with `SESSION_EXPIRED` code | [x] | Verified (primary + alias) |
| PATCH updates only provided fields | [x] | Confirmed via tests |
| Bio > 500 chars rejected (aligned spec) | [x] | Limit documented at 500; spec updated vs. former 512 |
| Migration test passes on clean DB (profile) | [~] | Available via profile; not default in pipeline |
| Registration stores hashed password | [x] | BCrypt encoder test via login/duplicate flows |

## 6. Acceptance Criteria — Frontend
| Criterion | Status | Notes |
|-----------|--------|-------|
| Login form authenticates & displays email | [ ] | Pending implementation |
| 401 from `/me` redirects to `/login` | [ ] | Pending |

---
## 7. Definition of Done Items
| DoD Item | Status | Notes |
|----------|--------|-------|
| All acceptance criteria met (backend scope) | [~] | Backend ✓, frontend pending |
| CI green with target thresholds | [x] | Gate 70% passing |
| No TODO/FIXME in auth/session code | [x] | None present |
| Docs updated (instructions/API/journal) | [~] | API & README ✓; journal & instructions.md pending |
| Flyway validate clean | [x] | Migrations apply without checksum issues |

---
## 8. Risks & Mitigations
| Risk Artifact (RISKS.md) Exists | Status |
|---------------------------------|--------|
| Separate risk tracking file | [ ] |

---
## 9. Guardrails & Policies
| Policy | Status | Notes |
|--------|--------|-------|
| No modification of applied migrations | [x] | Enforced by scripts + review |
| Every new endpoint has integration test | [x] | Covered |
| Standard error envelope everywhere | [x] | Implemented |
| Distinct `SESSION_EXPIRED` code | [x] | Implemented & tested |

---
## 10. Metrics / Targets
| Metric | Target | Status | Notes |
|--------|--------|--------|-------|
| Instruction coverage | ≥ 70% | [x] | Gate enforced & passing |
| Critical branch coverage | ≥ 80% (stretch) | [ ] | Not enforced |
| Mean local test time < 25s | Informational | [ ] | Not measured |
| Zero flaky retries | 0 | [ ] | Not tracked |

---
## 11. Stretch Goals (End of Week)
| Stretch | Status |
|---------|--------|
| Frontend profile edit form | [ ] |
| Session purge CLI / scheduler | [ ] |
| Audit log table | [ ] |

---
## 12. Summary of Key Gaps to Carry Into Week 3
1. Add migration enforcement to default CI (Testcontainers path).
2. Expand unit test coverage (services & edge cases) to reinforce 70% gate.
3. Implement minimal frontend login + `/me` display & redirect handling.
4. Create `instructions.md`, Week 2 journal, and `RISKS.md` file.
5. Consider AuthService abstraction & more granular logging / metrics.
6. Plan session pruning & optional rotation strategy.

---
*Checklist updated post-implementation to reflect actual Week 2 deliverables and remaining gaps.*
