# Automated Release System (Frontend) + Backend Adoption Blueprint

This document explains our end‑to‑end, automated release flow for the frontend and provides a pragmatic blueprint the backend can adapt. Treat the backend section as a starting point—not a mandate—so it can tailor steps to its build tool and infra.

---

## Frontend: End‑to‑End Flow

Primary goals:
- Reproducible, low‑friction versioning and tagging
- Clear CHANGELOG hygiene
- CI gates for quality (type/lint/tests/build, coverage visibility)
- Predictable post‑release steps (milestone and label hygiene, required checks)

### Components

- Versioning + CHANGELOG:
  - Script: `scripts/release.mjs`
  - Changelog: `CHANGELOG.md` (Keep‑a‑Changelog style)
- Tagging: annotated Git tags using `v` prefix (e.g., `v0.2.0`)
- CI:
  - `frontend-ci.yml` – lint, typecheck, tests, build, artifacts
  - `coverage-badge.yml` – runs coverage on PRs, writes badge on push to `main`, adds coverage percent to PR job summary
- Branch protection:
  - Require CI checks to pass (lint/type/tests/build)
  - Require “Coverage Badge” job (coverage on PRs) to pass on `main`
- Labels and triage aids:
  - `area:release` for changes to release mechanics or release branches
  - Milestones (e.g., `0.2.0`, `0.2.1`, `0.3.0`) to timebox scope

### Daily dev → PR → CI

1) Local quality gates (recommended):
```powershell
npm run typecheck ; npm run lint ; npm test ; npm run build
```
2) Open a PR targeting `main`:
- Include a checklist describing scope and any post‑merge actions (e.g., enabling a required check)
- If PR fixes an issue, add “Fixes #<n>” in PR description body
3) CI runs automatically:
- Lint, typecheck, tests, build
- Coverage reported in PR job summary; artifacts uploaded

### Cutting a release (maintainers)

Preconditions:
- `[Unreleased]` in `CHANGELOG.md` contains entries you intend to ship
- You’re on `main` and the working tree is clean
- Branch protection and required checks are green

Release (minor example):
```powershell
# Ensure up to date and clean
 git fetch origin ; git checkout main ; git pull

# Bump + tag (semver: choose --patch|--minor|--major)
 node scripts/release.mjs --minor --tag

# Push commit and tags
 git push
 git push --follow-tags
```
Notes:
- The script validates `[Unreleased]` content before bumping
- The script refuses to run unless you're on the `main` branch (require‑main guard)
- Going forward tags are v‑prefixed (e.g., `v0.2.0`)

### CHANGELOG rules of thumb
- All user‑visible or dev‑facing changes get a bullet under `[Unreleased]`
- The release script moves `[Unreleased]` → `[vX.Y.Z] - YYYY‑MM‑DD` and updates compare links
- Only shipped items belong in a released section; defer the rest to issues/milestones

### Post‑release checklist
- Triage leftovers: move open issues from the closed milestone to `0.2.1`/`0.3.0`; close the milestone
- Ensure required checks (coverage, CI) remain configured on `main`
- (Optional) Create a GitHub Release using the CHANGELOG section

### Tags and version metadata
- Tags: `vX.Y.Z` (annotated)
- App surfaces version/commit (via `VITE_APP_VERSION` / `VITE_COMMIT_HASH`) in the runtime debug panel/footer

---

## Backend: Adoption Blueprint (Adapt as Needed)

The backend can mirror the same principles while adapting to its stack (Maven/Gradle, container, etc.). Below is a suggested shape—feel free to modify.

### Goals
- Consistent semver + v‑prefixed tags
- CHANGELOG maintained alongside code
- CI running tests, building artifacts, and producing images (if containerized)
- Release flow that’s easy to audit and repeat

### Suggested components

- Version + CHANGELOG automation:
  - Add a `scripts/release.mjs` (or Kotlin/Groovy/Java equivalent) that:
    - Bumps version in `pom.xml`/`build.gradle`
    - Moves `[Unreleased]` → `[vX.Y.Z] - YYYY‑MM‑DD` in `CHANGELOG.md`
    - Commits and creates annotated Git tag `vX.Y.Z`
- CI pipelines:
  - `backend-ci.yml` job with: test, integration test (if any), build JAR/WAR, publish artifact
  - Optional: Build and push container image (GHCR/Docker Hub) tagged with `vX.Y.Z` and `latest`
  - (Optional) Security gates: Snyk/OWASP Dependency Check; fail on High/Critical
- Branch protection and required checks:
  - Require test/build (and security) to pass
  - Require release job for tags (or run on `area:release` PRs)

### Example release flow (backend)

1) Preconditions:
- `[Unreleased]` has entries to ship, tests are green
- Branch protection configured on `main`

2) Run release script (choose one):
```powershell
# Node script style, similar to frontend
 node scripts/release.mjs --minor --tag

# Or Gradle version bumper (example), then tag
 ./gradlew -Pversion=0.4.0 clean build
 git commit -am "chore(release): bump to 0.4.0"
 git tag -a v0.4.0 -m "Release v0.4.0"
```
3) Push commits and tags:
```powershell
git push ; git push --follow-tags
```
4) CI runs release pipeline:
- Build artifact(s), optionally build/push container images tagged `vX.Y.Z`
- (Optional) Publish GitHub Release and attach JAR/WAR checksums

### Suggested GitHub Action triggers
- On PRs to `main`: run tests and build; surface coverage/security summaries
- On tag push `v*`: run release pipeline (publish artifacts/images), generate Release notes from CHANGELOG section
- Optionally on PRs labeled `area:release`: run extra validation (CHANGELOG entry present, version consistent, on `main` only for tagging)

### Label & milestone alignment
- Reuse label semantics for filtering and automation:
  - `area:release` for release mechanics PRs
  - `type:fix`, `type:feature`, `typetooling`, `typedocs`, `typetest`
- Timebox changes with milestones (e.g., `0.3.0`) and close them post‑tag

---

## Backend Versioning & Release Flow

- Semantic Versioning (v0.1.0, v0.2.0, etc.) is used for all backend releases.
- Annotated tags trigger CI/CD workflow for build, test, and artifact publishing.
- Use /api/version to verify deployed backend version after release.
- See README and docs/instructions.md for full process.

## FAQ / Notes

- Do we have to implement this verbatim on the backend?
  - No—treat it as a template. Keep the core principles (semver + v‑tags, CHANGELOG discipline, CI gates), but change the scripting and CI details to fit Maven/Gradle and your runtime.
- What’s the minimum viable setup?
  - A release script that bumps version + updates CHANGELOG, Git tag with `vX.Y.Z`, and a CI job that builds + tests on PRs.
- How do we avoid accidental releases from feature branches?
  - Add a “require main” guard in the release script (check current branch before proceeding) and use branch protection. Frontend already enforces this in `scripts/release.mjs`.
- What about environment‑specific deployment?
  - Keep release (build/tag/publish) distinct from deployment. Deployment can be a separate workflow triggered manually or on Release creation.

---

## TL;DR (Frontend)

- Open PR → CI runs (lint/type/test/build); coverage is visible in PR summary
- On main, cut a release:
```powershell
node scripts/release.mjs --minor --tag ; git push ; git push --follow-tags
```
- Tag is `vX.Y.Z`; CHANGELOG updated; post‑release triage + milestone closure
- Coverage job is required on `main`; use `area:release` for release‑mechanics changes
