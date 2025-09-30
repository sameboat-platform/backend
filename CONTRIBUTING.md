# Contributing to SameBoat Backend

Welcome! This project uses a lightweight, guard‑railed workflow. Please read this entire page before opening a PR.

## 1. Start Here
Open (and keep open while prompting AI assistants):
- `.github/copilot-instructions.md` (core architecture + guard rails)
- `pom.xml`
- An example controller (`UserController`) and service (`UserService`)

That instructions file defines alias tokens you should prepend to AI prompts:
```
Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.
```

## 2. CI Guard (Critical)
Single authoritative workflow: `.github/workflows/backend-ci.yml`.
- If it exists: modify only that file for CI adjustments.
- If tooling/AI cannot see it: respond (or ensure AI responds) EXACTLY:
  `backend-ci.yml is missing. Please confirm before I generate any new CI workflow.`
Do **not** add `ci.yml`, `build.yml`, etc. unless explicitly approved.

## 3. Branch & Commit Style
- Branch naming: `feat/<short-desc>`, `fix/<issue>`, `chore/<scope>`
- Commit messages: Conventional style preferred (e.g. `feat(user): add deactivate endpoint`).
- One logical change per PR; keep diffs reviewable.

## 4. Layered Architecture (LAYER_RULE)
Controller → Service → Repository. No repository access directly from controllers. Keep business logic in services; controllers map DTOs.

## 5. Tests (Mandatory for New Logic)
Add at least:
- 1 unit test per new service method (mock repository)
- 1 integration test per new endpoint or security path
Maintain ≥ 70% instruction coverage (Jacoco gate enforces this during `mvn verify`).

## 6. Migrations (Flyway)
- Never edit an applied migration.
- Create new file: `V<next>__snake_case_description.sql` in `src/main/resources/db/migration`.
- Keep schema changes atomic; destructive changes require transitional steps.

## 7. Error Handling & Codes
Use / extend centralized handler `GlobalExceptionHandler`.
Existing codes: VALIDATION_ERROR, BAD_REQUEST, BAD_CREDENTIALS, UNAUTHENTICATED, SESSION_EXPIRED, INTERNAL_ERROR.
Add new domain exceptions + codes only with accompanying tests and update the catalog in `copilot-instructions.md` section 21.

## 8. Security (SECURITY_BASELINE)
- Validate all request DTOs (Jakarta Bean Validation).
- Avoid exposing sensitive internal details in error messages.
- Never log secrets or raw tokens.
- Enforce least privilege (avoid broad queries when specific ones suffice).

## 9. Local Development Quick Commands
```
# Full build + tests
./mvnw -B -ntp clean verify

# With migration container tests
./mvnw -Dskip.migration.test=false test
```
(Windows: use `mvnw.cmd`.)

## 10. PR Checklist (Self-Review Before Opening)
- [ ] Followed CI guard (no extra workflow files)
- [ ] Added/updated tests (positive + negative paths)
- [ ] Layering respected (no controller → repository shortcuts)
- [ ] DTOs validated; no entity leakage in responses
- [ ] Migration (if schema change) is new and atomic
- [ ] Error codes consistent & catalog updated if new one added
- [ ] No secrets or sensitive data added to logs/config

## 11. AI Assistance Guidelines
- Always prepend alias preamble (see Section 1) when using AI code generation.
- Open `copilot-instructions.md` so context is available.
- Reject suggestions that violate layering, security, or CI guard.

## 12. Getting Help
Open a draft PR early if change is sizable. Use discussions or issues for architectural questions. When in doubt, favor smaller, incremental PRs.

Thanks for contributing! 🚢
