# Week 3 Plan — SameBoat Honors Project (Fall 2025)

Status Input: Week 2 delivered hashed registration, distinct auth error codes, type‑safe config, prod profile, 70% coverage gate. Frontend auth UI & some docs (instructions.md, RISKS.md) still pending. JWT still future. This plan focuses on hardening auth, adding missing infra/tests, and delivering minimal end‑to‑end login UX.

---
## 1. Objectives (Week 3)
1. Harden authentication & session layer (complexity, rate limiting, pruning job).
2. Deliver minimal frontend auth experience (login, logout, display profile, redirect on 401).
3. Add migration & schema validation to default CI (Testcontainers) and raise unit coverage (target 75%).
4. Introduce formal risk tracking + instructions docs.
5. Prepare JWT feasibility path (spike & decision doc) without impacting current flow.

Stretch (if bandwidth permits): OpenAPI generation + typed client, initial session pruning scheduler, basic audit event persistence.

---
## 2. Scope & Deliverables
| Category | In-Scope (Committed) | Out of Scope (Deferred) |
|----------|----------------------|--------------------------|
| Auth | Password complexity validation; rate limiting (basic) | Full account recovery; MFA |
| Sessions | Expired session pruning (one scheduled job) | Session rotation / refresh tokens |
| Frontend | Login form, logout, /me display, redirect on 401 | Profile edit UI polish, settings page |
| Testing | Migration test in default CI; unit tests for services | Load/perf tests |
| Docs | instructions.md, RISKS.md, JWT spike notes | Full architectural whitepaper |
| Tooling | OpenAPI spike (generate spec + optional TS types) | Full client generation in build pipeline |

---
## 3. Detailed Task List
### 3.1 Backend Auth & Session Hardening
- Password complexity rule (min length 8, at least 1 upper, 1 lower, 1 digit) — configurable toggle if needed.
- Rate limiting: naive in‑memory bucket per email/IP (e.g., 5 attempts / 5 min) returning 429 `RATE_LIMITED`.
- Session pruning scheduled task (e.g., every hour) deleting expired entries.
- Add INFO log for rate-limit triggers.

### 3.2 Frontend Minimal Auth
- Create login page (email/password) + error display (BAD_CREDENTIALS, RATE_LIMITED).
- Store session implicitly via cookie; fetch `/me` on app mount.
- On 401 UNAUTHENTICATED or SESSION_EXPIRED redirect to /login.
- Display user email + logout button.

### 3.3 CI & Testing Enhancements
- Enable Testcontainers migration test in default `mvn verify` (or a matrix job) — remove reliance on H2 for schema-sensitive tests.
- Add unit tests: UserService (updatePartial, normalizeEmail), SessionService (touch & expiry logic), password validator.
- Increase coverage gate to 75% (if new tests raise actual coverage ≥ target). Fallback: keep gate 70% but add delta report.

### 3.4 Documentation & Governance
- Create `docs/instructions.md` (backend + frontend run, env var table, prod deploy steps).
- Create `docs/RISKS.md` (top 6 risks, impact, mitigation owner).
- JWT Spike: short `docs/spikes/jwt_vs_extended_sessions.md` with pros/cons, complexity, migration plan.

### 3.5 OpenAPI (Stretch)
- Add `springdoc-openapi-starter-webmvc-ui` dependency.
- Verify `/v3/api-docs` & Swagger UI (dev only).
- Export static spec into `openapi/merged.yaml`.
- (If time) generate TypeScript types (e.g., openapi-typescript).

### 3.6 Observability & Logging
- Structured log pattern (JSON) behind profile `prod` (optional toggle).
- Add login attempt counters (success/failure) as Micrometer counters.

---
## 4. Acceptance Criteria
| Area | Criterion | Code / Status Expectation |
|------|-----------|---------------------------|
| Password Validation | Reject weak password on register | 400 VALIDATION_ERROR with message substring e.g. `password complexity` |
| Rate Limiting | Exceed attempts returns 429 | JSON `{ error: "RATE_LIMITED" }` |
| Session Pruning | Expired sessions absent after job run | Integration test verifies deletion |
| Frontend Login | Successful login shows user email | DOM test + network mock or real backend dev env |
| Frontend Redirect | /me 401 triggers redirect to /login | Cypress / Vitest + jsdom scenario |
| Migration Test | Default CI pipeline runs Postgres container | Build log shows Testcontainers startup |
| Coverage | ≥ 75% instruction (or documented fallback) | JaCoCo check passes |
| Docs | instructions.md + RISKS.md exist & linked | README pointers added |
| JWT Spike | Spike doc with decision matrix committed | File merged to `docs/spikes` |

---
## 5. Sequencing (Proposed Order)
1. Password complexity + tests.
2. Rate limiting & tests.
3. Session pruning scheduler + test.
4. Migration test integration (adjust CI profile) + coverage raise.
5. Frontend minimal auth pages + redirect logic.
6. Documentation (instructions, risks).
7. JWT spike doc.
8. Stretch: OpenAPI + type generation; structured logging.

---
## 6. Definition of Done (Week 3)
- All acceptance criteria above met OR explicitly deferred with rationale.
- CI green with updated coverage gate (≥75%) or documented future raise plan.
- Production profile tested locally / ephemeral with secure cookie still functioning.
- End-to-end manual smoke (register → login → /me → logout) verified in dev + redirected 401 behavior confirmed.
- Risks documented & classified (Impact / Likelihood / Mitigation / Owner).
- Spike doc for JWT vs extended sessions merged.

---
## 7. Metrics Targets
| Metric | Target | Notes |
|--------|--------|-------|
| Instruction Coverage | 75% | Raise from 70% if feasible |
| Failed Login Rate Limit Trigger Test | Deterministic | Unit/integration coverage |
| Expired Session Prune Latency | < 5 min after expiry | Scheduler frequency tuning |
| Mean CI Build Time | Within +10% of Week 2 | Monitor after Testcontainers enable |

---
## 8. Risks (Initial for Week 3)
| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Rate limiter false positives | Medium | Low | Generous window + logging |
| Coverage raise delays feature work | Medium | Medium | Incremental unit tests early in week |
| JWT spike scope creep | Medium | Medium | Time-box spike (≤4h) & decision matrix |
| Session table growth before pruning | Medium | Medium | Implement pruning job early (Day 2) |
| Frontend auth delay | High | Medium | Parallelize FE tasks once backend changes merged |
| OpenAPI scope expansion | Low | Medium | Keep as stretch |

---
## 9. Stretch Goals
- OpenAPI spec + generated TS types consumed in FE build.
- Structured JSON logging (prod profile) + log correlation id.
- Basic audit table capturing login/logout events.

---
## 10. Out-of-Scope (Explicit)
- MFA / OAuth2 / Social login
- Password reset flows
- Full profile settings UI
- Multi-region database setup
- CDN edge cache customization beyond default

---
## Week 3 Completion Checklist
- [ ] Password complexity validation (min 8 chars, upper/lower/digit) implemented and tested
- [ ] Rate limiting (5 attempts/5 min per email/IP) returns 429 `RATE_LIMITED` and is tested
- [ ] Session pruning scheduled job deletes expired sessions; integration test verifies deletion
- [ ] INFO log for rate-limit triggers added
- [ ] Frontend login page with error display (BAD_CREDENTIALS, RATE_LIMITED)
- [ ] Session stored via cookie; `/me` fetched on app mount
- [ ] 401/SESSION_EXPIRED redirects to /login
- [ ] User email displayed with logout button
- [ ] Testcontainers migration test enabled in CI; coverage gate raised to 75% (or fallback documented)
- [ ] Unit tests for UserService (updatePartial, normalizeEmail), SessionService (touch & expiry logic), password validator
- [ ] Documentation: `docs/instructions.md` and `docs/RISKS.md` created and linked in README
- [ ] JWT spike doc: `docs/spikes/jwt_vs_extended_sessions.md` committed
- [ ] (Stretch) OpenAPI spec generated, static spec exported, TypeScript types generated
- [ ] (Stretch) Structured JSON logging (prod profile) and log correlation id
- [ ] (Stretch) Basic audit table for login/logout events
*Prepared at start of Week 3; adjustments allowed with documented rationale.*
