# Week 4 Plan – Draft (Working Document)

Date: 2025-10-02 (initial scaffold)
Theme (Proposed): Quality hardening, accessibility polish, developer ergonomics, incremental resilience.

## Objectives (Draft)

| Objective                             | Rationale                         | Success Criteria                                             |
| ------------------------------------- | --------------------------------- | ------------------------------------------------------------ |
| Expand automated auth & routing tests | Increase confidence for refactors | >90% of auth actions covered (login/register/refresh/expiry) |
| CI executes tests & fails on red      | Prevent regressions               | CI workflow includes `npm test` before build artifact upload |
| Health polling resilience             | Reduce noise, improve signal      | Pause-on-error toggle + exponential backoff implemented      |
| Accessibility & reduced motion        | Inclusive UX                      | Global reduced-motion flag disables route & panel animations |
| Environment onboarding                | Faster contributor setup          | `.env.example` committed & referenced in README              |
| Developer control utilities           | Faster iteration                  | Runtime debug panel exposes interval override (dev only)     |
| Visual polish (avatars)               | Clarity in demos                  | Deterministic identicon fallback implemented                 |
| Session freshness                     | Better long-lived tab UX          | Visibility change triggers conditional refresh               |

## Detailed Tasks (Initial Backlog)

### 1. Testing & CI

-   Add `npm run test -- --run` (or plain `npm test`) step to `frontend-ci.yml` after install & before build.
-   Add tests:
    -   Successful login redirect preserves target path
    -   BAD_CREDENTIALS displays mapped error
    -   Email validation errors (client-side) do not call network
    -   Session expiry mid-flow → protected route triggers redirect
    -   Refresh normalization for wrapped user object
-   Add coverage threshold config (optional) – decide if gating.

### 2. Health Polling Resilience

-   Introduce pause-on-error mechanism: on N consecutive failures, stop polling & surface resume button.
-   Add exponential backoff: intervals scale e.g. 30s, 60s, 120s (cap 5m) unless manually resumed.
-   Track failure streak + last successful timestamp in debug panel.

### 3. Accessibility / Motion Control

-   Central reduced-motion preference context (reads prefers-reduced-motion + user toggle persisted in localStorage).
-   Gate Framer Motion variants & debug panel collapse animation with preference.
-   Document accessibility approach in `architecture.md`.

### 4. Environment & Config

-   Create `.env.example` enumerating all `VITE_` vars with comments.
-   Update README section to link `.env.example` and remove inline encouragement placeholder.
-   Consider runtime warning if essential env var missing (dev only).

### 5. Debug Panel Enhancements

-   Add dev-only health interval override slider/input (min 5s, max 120s) – ephemeral (not persisted) or stored in localStorage.
-   Display failure streak & backoff status.
-   Add button: "Force full auth refresh" distinct from health ping.

### 6. Visual / UX Polish

-   Deterministic identicon: derive background & initial from user id hash (no external dependency) or small canvas generation.
-   Copy-to-clipboard user ID on Me page (button or icon next to ID).
-   Minor spacing sweep after new controls added.

### 7. Session Freshness Strategy

-   Listen to `visibilitychange`; if `hidden→visible` and last refresh > X minutes (configurable, default 5), trigger `refresh()`.
-   Avoid overlapping with ongoing auth action (skip if status `loading`).

### 8. Documentation

-   Add Week 4 section in `architecture.md` summarizing resilience & accessibility changes once implemented.
-   Update `week-3-plan-checklist.md` link references if new metrics script added to repo.
-   Maintain a short CHANGELOG segment in README (or dedicated file if scope grows).

## Stretch (If Core Finishes Early)

-   Introduce Zod-based form schema validation (replace ad-hoc field checks).
-   Feature flag scaffold (`useFeatureFlag`) reading from a JSON manifest (dev only initially).
-   Basic performance timing capture in debug panel (mount → bootstrap duration).

## Risks & Mitigations (Preliminary)

| Risk                                                | Impact                           | Mitigation                                                                       |
| --------------------------------------------------- | -------------------------------- | -------------------------------------------------------------------------------- |
| Over-scoping resilience (too many features at once) | Delays core testing improvements | Time-box health backoff to minimal viable feature (pause + linear backoff first) |
| Flaky network tests in CI                           | False negatives slow PRs         | Use deterministic fetch mocks; isolate timing logic                              |
| Reduced-motion toggle drift                         | Inconsistent motion states       | Central provider + single hook gating all variants                               |

## Definition of Done (Draft)

-   All objectives have measurable artifact (tests, code, docs) merged to `main`.
-   CI pipeline red on any test failure.
-   Health poll can pause & resume with visible state.
-   Reduced-motion toggle persists between reloads and disables major animations.
-   `.env.example` present and referenced.

---

Status: DRAFT – to refine with team feedback.
Maintainer: Frontend Team
