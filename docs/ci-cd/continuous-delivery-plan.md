# Continuous Delivery Plan — SameBoat Backend (Spring Boot 3 + Flyway + Render)

Applies BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE. This plan describes how we will introduce semantic versioning for the backend artifact and move from basic CI to Continuous Delivery (manual approval before production deploy) without violating our single-workflow rule.

Status: Week 3 (backend supportive week). Scope is process + pipeline + docs; minimal code change required.

---
## 1) Goals
- Per-repo semantic versioning for backend artifact (independent of frontend).
- Single authoritative CI workflow (`.github/workflows/backend-ci.yml`).
- On every commit/PR: build + tests + coverage + migration checks.
- On push to `main`: build + tests, create/push Docker image, then gated production deploy (manual approval).
- Safe Flyway migrations and clear rollback path.

---
## 2) Constraints & Guardrails
- BACKEND_CI_GUARD: Only modify `.github/workflows/backend-ci.yml` if it exists. Do NOT add additional workflows. If the file appears missing in your editor, verify on disk before creating anything new.
- LAYER_RULE: No controller↔repository shortcuts. Not directly relevant here but keep in tests/examples.
- SECURITY_BASELINE: No secrets in repo. Use GitHub Environments and Render secrets.

---
## 3) Versioning Model (Artifact vs API)
- Artifact version (Maven `pom.xml`) uses SemVer (0.y.z while pre-1.0). Bump:
  - PATCH: bug fix
  - MINOR: backward-compatible features
  - MAJOR: breaking changes (rare pre-1.0)
- API versioning is separate (e.g., `/api/...` unversioned for now). Avoid breaking contracts; when needed, plan `/api/v1`.

Commands (Windows):
```
mvnw.cmd -ntp versions:set -DnewVersion=0.1.0
mvnw.cmd -ntp versions:commit
# Optional tag
git tag -a backend-v0.1.0 -m "Backend 0.1.0"
git push origin backend-v0.1.0
```

---
## 4) Environments & Secrets (GitHub + Render)
- GitHub Environments:
  - Create `dev` and `prod` in GitHub → Settings → Environments.
  - For `prod`, require reviewers (manual approval gate).
  - Store secrets in environments (not repository-level):
    - If deploying Docker image: `GHCR_USERNAME`, `GHCR_TOKEN` (or rely on `GITHUB_TOKEN` with `packages: write`), optional `IMAGE_REGISTRY`.
    - Render API: `RENDER_API_KEY`, `RENDER_SERVICE_ID`.
- Render:
  - Option A (simple CD): keep GitHub-connected repo, disable auto-deploy on `main`; CI approval triggers Render deploy via API.
  - Option B (preferred medium-term): Render service uses Docker image from GHCR; CI publishes image and triggers deploy/update.

---
## 5) Pipeline Shape (Single Workflow)
Edit `.github/workflows/backend-ci.yml` only.

Jobs:
1) build (on PR + push):
   - Checkout
   - Setup Java 21
   - `./mvnw -B -ntp clean verify`
   - Enforce JaCoCo gate (≥70%) and run migration tests (Testcontainers) per project settings.
2) docker (on push to main, needs build):
   - Log in to GHCR
   - Build Docker image tagged with `backend:<pom-version>` and `backend:sha-<shortsha>`
   - Push image
3) deploy-prod (on push to main, needs docker, environment: prod):
   - Waits for manual approval in GitHub Environments (prod)
   - Calls Render Deploy API using `RENDER_API_KEY`/`RENDER_SERVICE_ID`, referencing the pushed image (Option B) or latest commit (Option A)

Notes:
- Keep workflow matrix lean to maintain build times.
- No additional YAML files.

---
## 6) Flyway Strategy (Safe Migrations)
- Keep migrate-on-start enabled unless/until we split migration into a dedicated job.
- Author migrations with expand/contract pattern to avoid downtime and allow quick rollback of app code.
- Never edit an applied migration; always create a new `V<next>__description.sql`.
- For complex changes, add a Testcontainers-backed migration test.

Optional future step:
- Disable Flyway on prod app start and run `flyway:migrate` in a gated CI step before deployment. For Week 3 we keep current startup migration behavior.

---
## 7) Rollback Plan
- App rollback: redeploy previous Docker image tag from GHCR (e.g., `backend:sha-<prev>` or previous SemVer tag).
- DB rollback: rely on backups/snapshots; avoid destructive migrations; use expand/contract so rolling back app does not require DB rollback.
- Keep a short “last-good” record per deploy in release notes.

---
## 8) Verification & Quality Gates
- Build must pass `mvnw.cmd -ntp clean verify` locally and in CI.
- Coverage ≥ 70% (can raise to 75% when realistic this week).
- Migration tests run and pass (Testcontainers where configured).
- After deploy approval, verify:
  - Health: `GET https://api-sameboat.onrender.com/health` or `/api/actuator/health` (depending on current mapping)
  - Auth flow: register → login → `/api/me` → logout (dev: localhost; prod: Render domain)

---
## 9) Transition Steps (High-Level)
1) Set artifact version in `pom.xml` (start at 0.1.0) and tag.
2) Create GitHub Environments `dev` and `prod` with required reviewers for `prod`.
3) Add environment secrets (GHCR + Render) as described.
4) Update the single workflow (`backend-ci.yml`) to add `docker` and `deploy-prod` jobs.
5) In Render, set service to use GHCR image (Option B) or keep repo deploy with auto-deploy off (Option A).
6) Run a full pipeline on a test commit to `main` and approve the prod deployment.
7) Document image tag and release notes; confirm smoke tests in prod.

---
## 10) PowerShell curl tips (for manual checks)
- Use `--data-binary "@file.json"` to avoid quoting issues.
- Or use: `@'{"key":"value"}'@ | Set-Content body.json -Encoding utf8`
- Keep headers simple: `-H "Content-Type: application/json"`.

---
## 11) Ownership & RACI (Lightweight)
- Owner: Backend maintainers (sameboat-platform/backend)
- Approvers (prod environment): Project lead + 1 reviewer
- Operators: Anyone on-call can trigger rollback to previous image tag

---
## 12) References
- Render Deploy API: https://render.com/docs/deploy-hooks-and-api
- GHCR: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry
- Flyway: https://flywaydb.org/documentation/
- JaCoCo: https://www.jacoco.org/jacoco/
