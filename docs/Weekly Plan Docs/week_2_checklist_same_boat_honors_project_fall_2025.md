# Week 2 Checklist — SameBoat (Fall 2025)

Legend:
- [x] = Completed this week
- [ ] = Not completed (or only partially complete) — notes indicate gaps

> Source Plan: `week_2_plan_same_boat_honors_project_fall_2025_revised.md`

---
## 1. Scope (Committed Items)
| Task | Status | Notes |
|------|--------|-------|
| Flyway V2 finalized (users + sessions) | [x]    | `V2__users_and_sessions.sql` present |
| Migration immutability **rules** (policy + enforcement) | []    | Policy in README; no automated check scripts yet |
| `/auth/login` endpoint | [x]    | Implemented with dev stub password = "dev" |
| `/auth/logout` endpoint | [x]    | Invalidates (deletes) session + clears cookie |
| `GET /me` endpoint | [x]    | Returns `UserDto` or 401 error envelope |
| `PATCH /me` endpoint | [x]    | Partial update working; empty body allowed (plan wanted reject) |
| Opaque session token (SBSESSION cookie) | [x]    | Uses session UUID (no separate token column) |
| Validate session per request | [x]    | Custom `SessionAuthenticationFilter` |
| Minimal user profile fields | [x]    | email, displayName, avatarUrl, bio, timezone, role |
| Purge OR ignore expired sessions (lookup) | [x]    | Ignored if expired; no purge job |
| Backend tests: unit + MockMvc + Testcontainers Postgres | [ ]    | Only MockMvc + H2; migration test (skip) uses Testcontainers; no unit tests |
| Frontend: login form + display user info | [ ]    | Not implemented this week |
| CI: Postgres service in pipeline | [ ]    | Not added; tests rely on H2 locally |
| CI: Coverage gate ≥ 70% | [ ]    | Gate exists at 60% (raised from 35%); below target |
| Docs: run instructions updated | [x]    | README updated (no separate instructions.md) |
| Docs: API contract (`api.md`) | [x]    | Added with auth + profile slice |
| Milestone journal skeleton (Week 2) | [ ]    | Journal file for week 2 not created |

---
## 2. Deferred (Explicitly Out of Scope) — Confirmed Unimplemented
| Task | Status |
|------|--------|
| JWT issuance / verification | [ ] |
| Registration & password hashing | [ ] |
| Profile picture upload | [ ] |
| Rich validation (beyond basic lengths) | [ ] |
| Admin endpoints / user listing | [ ] |
| Session cleanup / rotation strategy | [ ] |
| Frontend profile edit form | [ ] |

---
## 3. Stretch (Not Required) — Attempted?
| Stretch Item | Status | Notes |
|--------------|--------|-------|
| Frontend PATCH profile form | [ ] | Not started |
| Expired session purge command/test | [ ] | Only rejection test implemented |
| Simple audit log (login/logout events) | [ ] | Not implemented |

---
## 4. Sequenced Workstream (Plan vs Completion)
| Step | Description | Status | Notes |
|------|-------------|--------|-------|
| 1 | DB & migration hardening | [ ] | V2 done; no immutability automation / migration test enabled |
| 2 | Entities/Repositories | [x] | User + Session; sessionToken field omitted (UUID id used) |
| 3 | Session token issuance + lookup service | [x] | `SessionService` + filter |
| 4 | `/auth/login` + test | [x] | Success + failure cases covered |
| 5 | `/me` GET + test | [x] | Auth + no-auth tests |
| 6 | `/auth/logout` + test | [x] | Invalidation verified |
| 7 | `PATCH /me` + test | [x] | Happy & validation failure tests |
| 8 | Frontend login + display | [ ] | Not started |
| 9 | CI enhancements (Postgres, coverage ≥70%) | [ ] | Coverage gate 60%, no Postgres service |
| 10 | Docs & journal updates, risk review | [ ] | API + README done; journal & RISKS.md missing |

---
## 5. Detailed Tasks Breakdown
### 5.1 Database & Flyway
- [x] Confirm V2 schema content
- [ ] Migration test (active & passing — current test skipped / uses H2)
- [x] README immutability note
- [ ] Automated immutability check (script / CI hook)

### 5.2 Domain Model
- [x] `UserEntity` with required fields
- [ ] Session separate `sessionToken` column (UUID id reused instead)

### 5.3 Auth & Sessions
- [ ] Dedicated `AuthService` abstraction (logic split across controller & services)
- [x] Dev password check == "dev"
- [x] Opaque token created
- [x] 7-day expiry logic
- [x] Filter performs validation & sets SecurityContext

### 5.4 SecurityConfig
- [x] Public endpoints configured
- [x] Protected endpoints enforced
- [x] Stateless configuration
- [x] `AuthPrincipal` set

### 5.5 Controllers
- [x] POST `/auth/login`
- [x] POST `/auth/logout`
- [x] GET `/me`
- [x] PATCH `/me`
- [ ] Reject empty PATCH body (currently a no-op accepted)

### 5.6 Observability
- [ ] INFO login event log
- [ ] WARN invalid/expired session log (debug only present)

### 5.7 Testing Strategy
- [ ] Unit tests (services / edge cases)
- [x] Integration: login success & failure
- [x] Integration: `/me` auth & no-auth
- [x] Integration: PATCH success
- [x] Integration: PATCH validation failure
- [x] Integration: logout invalidates
- [x] Integration: expired session 401
- [ ] Migration test (enabled & asserting constraints)
- [ ] Testcontainers Postgres for main integration suite

### 5.8 CI Enhancements
- [ ] Postgres service or enforced Testcontainers
- [x] Coverage gate present
- [ ] Coverage threshold meets 70% target
- [ ] Separate Flyway validate stage in pipeline

### 5.9 Docs
- [ ] `instructions.md` (distinct file)
- [x] `api.md` with endpoint specs
- [ ] Journal (Week 2) skeleton
- [ ] `RISKS.md` extracted (risks embedded only in plan)

---
## 6. Acceptance Criteria — Backend
| Criterion | Status | Notes |
|-----------|--------|-------|
| Login sets HttpOnly cookie & usable for `/me` | [x] | Verified in tests |
| Expired session 401 with `SESSION_EXPIRED` code | [ ] | Returns `UNAUTHORIZED` (code mismatch) |
| PATCH updates only provided fields | [x] | Confirmed via integration test |
| Bio > 512 chars rejected (400) | [ ] | Current limit enforced at 500; test for >512 not explicit |
| Migration test passes on clean DB | [ ] | Not active |

## 6. Acceptance Criteria — Frontend
| Criterion | Status | Notes |
|-----------|--------|-------|
| Login form authenticates & displays email | [ ] | Not implemented |
| 401 from `/me` redirects to `/login` | [ ] | Not implemented |

---
## 7. Definition of Done Items
| DoD Item | Status | Notes |
|----------|--------|-------|
| All acceptance criteria met | [ ] | Several unmet (frontend + codes + migration test) |
| CI green with target thresholds | [ ] | Coverage threshold below target |
| No TODO/FIXME in auth/session code | [x] | None introduced |
| Docs updated (instructions/API/journal) | [ ] | Journal + instructions.md missing |
| Flyway validate clean | [x] | Migrations load without checksum errors |

---
## 8. Risks & Mitigations
| Risk Artifact (RISKS.md) Exists | Status |
|---------------------------------|--------|
| Separate risk tracking file | [ ] |

---
## 9. Guardrails & Policies
| Policy | Status | Notes |
|--------|--------|-------|
| No modification of applied migrations | [x] | Observed; not automated |
| Every new endpoint has integration test | [x] | All backend endpoints covered |
| Standard error envelope everywhere | [x] | Implemented (401/400/500) |
| Distinct `SESSION_EXPIRED` code | [ ] | Not implemented |

---
## 10. Metrics / Targets
| Metric | Target | Status | Notes |
|--------|--------|--------|-------|
| Instruction coverage | ≥ 70% | [ ] | Gate at 60% (actual >60 but threshold below target) |
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
1. Add `SESSION_EXPIRED` error code path for expired sessions.
2. Introduce migration immutability automation (script + CI step).
3. Enable active migration test (Testcontainers Postgres) in regular CI.
4. Raise coverage gate toward 70% after supplementing unit tests.
5. Implement minimal frontend login + `/me` display.
6. Create `instructions.md`, Week 2 journal, and `RISKS.md` file.
7. Optional: Add AuthService abstraction + logging (INFO/WARN) per plan.
8. Align bio length limit with plan (512) or update plan/docs to 500.

---
*Prepared automatically based on implemented code & original Week 2 revised plan.*

