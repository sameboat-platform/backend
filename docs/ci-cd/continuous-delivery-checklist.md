# Continuous Delivery Checklist — SameBoat Backend

This is a practical, step-by-step checklist to move the backend to Continuous Delivery while keeping a single CI workflow per BACKEND_CI_GUARD. Use alongside `docs/ci-cd/continuous-delivery-plan.md`.

Legend: [ ] to-do, [x] done

---
## A. Repo Prep (Local)
- [ ] Ensure clean working tree (commit/stash local changes)
- [ ] Run full build locally
```
mvnw.cmd -ntp clean verify
```
- [ ] Set artifact version (SemVer; start with 0.1.0 if unset)
```
mvnw.cmd -ntp versions:set -DnewVersion=0.1.0
mvnw.cmd -ntp versions:commit
```
- [ ] Tag release (optional initial tag)
```
git tag -a backend-v0.1.0 -m "Backend 0.1.0"
git push origin backend-v0.1.0
```

---
## B. Environments & Secrets (GitHub)
- [ ] Create GitHub Environments: `dev` and `prod` (Settings → Environments)
- [ ] For `prod`, require reviewers (manual approval gate)
- [ ] Add environment secrets as needed:
  - GHCR (if pushing images): `GHCR_USERNAME`, `GHCR_TOKEN` or rely on `GITHUB_TOKEN` with `packages: write`
  - Render: `RENDER_API_KEY`, `RENDER_SERVICE_ID`

---
## C. Single CI Workflow Update (BACKEND_CI_GUARD)
- [ ] Open `.github/workflows/backend-ci.yml` (do NOT create new workflow files)
- [ ] Ensure `build` job runs on PR + push with:
  - Checkout
  - Setup Java 21
  - `./mvnw -B -ntp clean verify`
  - Fails on test/coverage/migration errors
- [ ] Add `docker` job (on push to main; `needs: build`):
  - Login to GHCR
  - Build Docker image tagged `ghcr.io/<org>/<repo>/backend:<pom-version>` and `:sha-<shortsha>`
  - Push image
- [ ] Add `deploy-prod` job (on push to main; `needs: docker`; `environment: prod`):
  - Waits for manual approval
  - Calls Render deploy API (deploy latest image or commit depending on service mode)

Note: Keep everything in the single workflow file per policy.

---
## D. Render Configuration
Choose one path:

Option A — Repo-based deploy (simpler now)
- [ ] In Render, keep the repo-connected service
- [ ] Disable auto-deploy on `main`
- [ ] CI’s `deploy-prod` job triggers Render deploy via API after approval

Option B — Image-based deploy (preferred later)
- [ ] Switch Render service to pull from GHCR image
- [ ] CI builds and pushes image; `deploy-prod` tells Render to deploy the new tag

---
## E. Profiles & Env Vars (Dev vs Prod)
Dev (local):
- [ ] Start backend with profile `dev` (e.g., `SPRING_PROFILES_ACTIVE=dev`)
- [ ] SAMEBOAT_CORS_ALLOWED_ORIGINS should include `http://localhost:5173`
- [ ] Do NOT set `SAMEBOAT_COOKIE_DOMAIN` (host-only cookie on localhost)
- [ ] Ensure `SAMEBOAT_COOKIE_SECURE` is `false` or unset (HTTP allowed locally)

Prod (Render):
- [ ] Start with profile `prod` (Render environment variable `SPRING_PROFILES_ACTIVE=prod`)
- [ ] Set `SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboatplatform.org` (no wildcard)
- [ ] Set `SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org` and `SAMEBOAT_COOKIE_SECURE=true`
- [ ] Ensure Postgres env vars are present; do not hardcode credentials

When ready to stop relying on Render’s ad-hoc env vars:
- [ ] Remove any dev-only values from Render (e.g., localhost origins, cookie secure=false)
- [ ] Keep only prod-appropriate values in Render (see above)

---
## F. Verification Steps (Before Enabling CD)
- [ ] Push a branch and open a PR; confirm `build` job runs and passes
- [ ] Merge to `main` (or push to `main` temporarily) to trigger `build` + `docker`
- [ ] Confirm image pushed to GHCR with both version and SHA tags
- [ ] Approve `deploy-prod` in GitHub Environments and confirm Render deploy completes

Smoke tests (dev):
```
# Register (PowerShell-safe)
@'{
  "email":"dev-user@example.com",
  "password":"Passw0rd!",
  "displayName":"Dev User"
}'@ | Set-Content reg.json -Encoding utf8

curl.exe -i -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  --data-binary "@reg.json"

# Login
@'{
  "email":"dev-user@example.com",
  "password":"Passw0rd!"
}'@ | Set-Content login.json -Encoding utf8

curl.exe -i -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  --data-binary "@login.json"

# Me (cookie-based session must be retained by your client/tool)
```
Note: For cookie session testing, prefer a browser or a REST client that preserves cookies (curl.exe does not store cookies by default; use `-c`/`-b` for a cookie jar if needed).

---
## G. Rollback Drill
- [ ] Identify last-good Docker image tag (e.g., `sha-<prev>`)
- [ ] Trigger Render deploy of previous tag
- [ ] Verify health and /api/me behavior

---
## H. Documentation
- [ ] Read `docs/ci-cd/continuous-delivery-plan.md`
- [ ] Link the plan + checklist from `README.md` (optional)
- [ ] Add release notes mapping FE tag to BE tag (optional)

---
## I. Ready to Flip the Switch
- [ ] Confirm tests/coverage stable, migration tests green
- [ ] Keep manual approval (Continuous Delivery) for this week
- [ ] Revisit auto-deploy (Continuous Deployment) after adding more safety nets

