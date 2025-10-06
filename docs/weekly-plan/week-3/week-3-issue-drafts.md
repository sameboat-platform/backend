# SameBoat Backend – Week 3 Issue Drafts

This document contains ready-to-copy issue drafts for GitHub, covering all tasks required to complete Week 3 for the SameBoat backend. Each item is formatted for direct use as a GitHub issue.

---

## 1. Confirm Auth Endpoints and Error Codes
**Title:** Confirm /api/auth/* endpoints and error code consistency
**Description:**
- Review and document all exposed authentication endpoints: POST /api/auth/login, POST /api/auth/register, POST /api/auth/logout, GET /api/me.
- Ensure error codes returned include: BAD_CREDENTIALS, UNAUTHENTICATED, SESSION_EXPIRED, EMAIL_EXISTS, VALIDATION_ERROR.
- Add/adjust tests to verify error code responses for each endpoint.
  Labels: `api`, `backend`, `security`, `typetest`, `roadmap`, `area:auth`
  Milestone: v0.1.x
---

## 2. Tighten SecurityConfig
**Title:** Remove legacy /actuator/* permitAll from SecurityConfig
**Description:**
- Update SecurityConfig to remove any permitAll rules for /actuator/*.
- Ensure only /api/actuator/* is permitted where needed.
- Add/adjust tests to confirm correct access control.
  Labels: `security`, `backend`, `infra`, `typetest`, `roadmap`, `area:auth`
  Milestone: v0.1.x
---

## 3. Add /api/actuator/info Test
**Title:** Add integration test for /api/actuator/info endpoint
**Description:**
- Implement a test to verify /api/actuator/info is reachable and returns expected info payload.
- Confirm endpoint is protected as required by security rules.
  Labels: `api`, `typetest`, `infra`, `backend`, `area:health`
  Milestone: v0.1.x
---

## 4. CORS Settings for Frontend Integration
**Title:** Review and update CORS settings for frontend integration
**Description:**
- Ensure CORS allows http://localhost:5173 for dev and https://app.sameboatplatform.org for prod.
- Confirm Access-Control-Allow-Credentials is true and cookies are set correctly for both profiles.
- Document any changes for frontend agent reference.
  Labels: `security`, `infra`, `ui/ux`, `roadmap`, `backend`, `area:auth`
  Milestone: v0.1.x
---

## 5. Profile-Specific Configurations
**Title:** Implement dev and prod profiles for environment-specific settings
**Description:**
- Create/verify dev and prod Spring profiles.
- For dev: omit SAMEBOAT_COOKIE_DOMAIN, set SAMEBOAT_COOKIE_SECURE=false, allow CORS from http://localhost:5173.
- For prod: set SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org, SAMEBOAT_COOKIE_SECURE=true, allow CORS from https://app.sameboatplatform.org.
- Document profile switching and environment variable usage.
  Labels: `infra`, `roadmap`, `backend`, `security`, `area:runtime`
  Milestone: v0.1.x
---

## 6. Database Migration: Add timezone Column
**Title:** Add timezone column to users table via Flyway migration
**Description:**
- Create a new Flyway migration to add timezone column to users table.
- Ensure migration is atomic and does not modify previous migrations.
- Add/adjust tests to verify schema change.
  Labels: `db`, `enhancement`, `roadmap`, `backend`, `area:db`
  Milestone: v0.1.x
---

## 7. Fix Startup Errors Related to DB Schema
**Title:** Fix startup errors due to missing timezone column
**Description:**
- Investigate and resolve startup errors caused by missing timezone column in users table.
- Confirm migration is applied and app starts without errors.
  Labels: `db`, `bug`, `backend`, `help wanted`, `area:db`
  Milestone: v0.1.x
---

## 8. Versioning and Milestone Setup
**Title:** Set backend artifact version and create milestones
**Description:**
- Set backend version to v0.1.0 (initial) and plan for v0.2.0 (week 3).
- Create GitHub milestones and tags using v prefix.
- Document release process and versioning conventions.
  Labels: `infra`, `roadmap`, `documentation`, `backend`, `area:release`, `area:project`
  Milestone: v0.2.0
---

## 9. CI/CD Workflow Review
**Title:** Review and update backend CI/CD workflow
**Description:**
- Ensure .github/workflows/backend-ci.yml is the only workflow file.
- Confirm build, test, migration check, and deploy steps are present.
- Add steps for tag-based deploy and auto-close issues on PR merge if needed.
  Labels: `infra`, `github_actions`, `area:ci`, `area:release`
  Milestone: v0.2.0
---

## 10. Documentation Updates
**Title:** Update backend documentation for Week 3 changes
**Description:**
- Update README.md, api.md, and architecture notes to reflect new endpoints, error codes, CORS settings, and profile usage.
- Document release and deployment process for future reference.
  Labels: `documentation`, `typedocs`, `area:project`
  Milestone: v0.2.0
---

## 11. Cross-Repo Coordination
**Title:** Link backend milestones and issues to frontend project
**Description:**
- Reference backend milestones/issues in frontend repo where relevant.
- Ensure cross-repo visibility for features and bug fixes impacting both sides.
  Labels: `documentation`, `question`, `area:project`
  Milestone: v0.2.0
---

## 12. Final Review and QA
**Title:** Final review and QA for Week 3 backend deliverables
**Description:**
- Review all completed issues and PRs for Week 3.
- Confirm all tests pass and coverage meets minimum requirements.
- Complete reflection and journal notes for Week 3.
- Complete a week-3-exit-report.md in docs/exit-reports/ that includes overview, highlights, what went well, what we struggled with, completed vs planned, notable PRs/commits, metrics and quality gates, and follow-up actions.
  Labels: `typetest`, `documentation`, `area:release`, `area:project`
  Milestone: v0.2.0
---

## 13. Implement Password Complexity Validation
**Title:** Implement password complexity validation for registration
**Description:**
- Enforce password rules: minimum length 8, at least 1 uppercase, 1 lowercase, and 1 digit.
- Map violations to 400 VALIDATION_ERROR with clear message.
- Add unit/integration tests for weak/valid password cases.
- Document rule in API docs.
  Labels: `api`, `security`, `typetest`, `area:auth`
  Milestone: v0.1.1
---

## 14. Add Basic Rate Limiting to Auth Endpoints
**Title:** Add basic rate limiting to authentication endpoints
**Description:**
- Implement naive in-memory rate limiting (e.g., 5 attempts per 5 min per email/IP).
- Return 429 RATE_LIMITED error code when exceeded.
- Add INFO log for rate-limit triggers.
- Add unit/integration tests for rate limit logic.
  Labels: `api`, `security`, `typetest`, `area:auth`
  Milestone: v0.1.1
---

## 15. Implement Session Pruning Scheduled Task
**Title:** Implement scheduled task to prune expired sessions
**Description:**
- Add scheduled job (e.g., hourly) to delete expired session entries from DB.
- Add integration test to verify expired sessions are removed.
- Document job in API/architecture docs.
  Labels: `db`, `enhancement`, `typetest`, `area:db`, `area:auth`
  Milestone: v0.1.1
---

## 16. Ensure Migration Test Runs in CI (Testcontainers)
**Title:** Ensure migration test runs in CI using Testcontainers
**Description:**
- Update CI pipeline to run migration test with Postgres Testcontainers (not H2).
- Verify build log shows Testcontainers startup and migration test pass/fail.
- Document process in instructions.md.
  Labels: `infra`, `db`, `github_actions`, `area:ci`, `area:db`
  Milestone: v0.1.1
---

## 17. Add/Improve Unit Tests for UserService, SessionService, Password Validator
**Title:** Add/improve unit tests for UserService, SessionService, and password validator
**Description:**
- Add/expand unit tests for UserService (updatePartial, normalizeEmail), SessionService (touch & expiry logic), and password validator.
- Raise coverage to ≥75% if feasible; fallback: keep gate at 70% and add delta report.
- Document coverage status in README.
  Labels: `typetest`, `area:auth`, `area:db`
  Milestone: v0.1.1
---

## 18. Create and Link instructions.md and RISKS.md
**Title:** Create and link instructions.md and RISKS.md documentation
**Description:**
- Create docs/instructions.md (run steps, env vars, deploy, frontend integration).
- Create docs/RISKS.md (top 6 risks, impact, mitigation owner).
- Link both docs from README and project board.
  Labels: `documentation`, `area:project`
  Milestone: v0.1.1
---

## 19. Prepare JWT Spike Decision Doc
**Title:** Prepare JWT spike decision doc (pros/cons, migration plan)
**Description:**
- Create docs/spikes/jwt_vs_extended_sessions.md with pros/cons, complexity, migration plan.
- Time-box spike (≤4h) and add decision matrix.
- Link doc from README and project board.
  Labels: `documentation`, `area:project`, `security`
  Milestone: future
---

## 20. Final Review and QA -> v0.2.0
**Title:** Final review and QA for v0.2.0 backend deliverables
**Description:**
- Review all completed issues and PRs for v0.1.x and v0.2.0.
- Confirm all tests pass and coverage meets minimum requirements.
- Prepare release notes for v0.2.0 milestone.
  Labels: `typetest`, `documentation`, `area:release`, `area:project`
  Milestone: v0.2.0

---