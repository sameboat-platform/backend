# Week 2 Checkout Summary

## Overview (Week 2 - Sept 22 - 28):
**Spilled over into 30th**
1. ### Deployment & Runtime Readiness
   - Added a production-ready multi-stage Dockerfile (non-root user, memory tuning, `$PORT` support).
   - Introduced .dockerignore to shrink build context.
   - Expanded `README.md` with container usage, production hosting snapshot, and environment variable guidance.
2. ### Configuration & Environment Hardening
   - Clarified production domains and canonical API/frontend origins.
   - Updated application-prod.yml with correct cookie domain, secure flag, session TTL, and refined CORS origin list.
   - Added actuator base path (`/api/actuator`) for consistency with frontend routing and external monitors.
3. ### CORS & Public Endpoint Stability
   - Reworked CORS wiring: explicit `CorsConfig`, security chain `.cors()`, permitted preflight OPTIONS, added origin logging, and transitioned from misconfigured env overrides to a single source of truth.
   - Added integration coverage for allowed vs disallowed preflight requests.
   - Exposed `/api/actuator/health` (and `info`) publicly while keeping business endpoints secured.
4. Security & Layering Integrity
   - Preserved service/controller boundaries while expanding security config; no controllers were given persistence shortcuts.
   - Ensured only narrowly necessary endpoints became permitAll (`/health` + `/auth`).
5. ### Testing & Quality
   - Added new integration tests: CORS behavior, public health endpoint.
   - Adjusted failing test expectations to reflect real preflight semantics (`403 for disallowed origin`).
   - Removed noisy H2/PostgreSQL dialect mismatch warning by dropping forced dialect in test profile.
   - Build remains green with prior auth + user tests unchanged.
6. ### Developer Experience & Observability
   - Startup logging of effective CORS origins for fast prod debugging.
   - `README`, `/docs`, and instructions now reflect consistent deployment narrative (Neon, Render, Netlify).
7. ### Cleanup & Consistency
   - Eliminated placeholder <tld> domains in prod config.
   - Unified actuator exposure strategy; removed confusion between legacy and base-path endpoints.
   - Avoided broadening workflows or CI footprint (respected `BACKEND_CI_GUARD`).
## Key Decisions & Rationale:
- Chose to keep `/api/actuator/health` as the canonical public liveness endpoint to align with future reverse proxies and keep root path surface area minimal.
- Did not introduce wildcard origins—explicit whitelisting reduces risk and simplifies audit.
- Added logging instead of temporary debug endpoints—non-intrusive and zero extra attack surface.
- Allowed both legacy `/actuator/*` and new `/api/actuator/*` in security rules initially, then validated we only need the new path (test adjusted accordingly).

  - ### What Went Well:
     We moved from an ad‑hoc local-only setup to a deployable, production-aligned backend with clear domain and hosting semantics. Shipping the `Dockerfile` plus tightened prod config gave us a clean deploy story. The `CORS` troubleshooting loop was handled systematically: reproduce, isolate profile/env mismatch, add logging, add tests, then simplify. Tests acted as guard rails—every change (`CORS`, health access, security tweaks) gained coverage, so refactors stayed safe. Documentation (`README.md` + `copilot-instructions.md`) evolved in-step, reducing future onboarding friction. Overall, we increased operational clarity without compromising security boundaries.
- ### What We Struggled With:
   The primary friction point was `CORS` misconfiguration via environment variable overrides that silently masked YAML values—diagnosis required adding runtime visibility. A secondary nuisance was actuator path ambiguity (`/actuator/health` vs `/api/actuator/health`), which caused a failing test until we aligned expectations. The PostgreSQL dialect warning noise in H2 wasn’t harmful but distracted from meaningful logs. Finally, iterative security config edits risked drift; we had to be deliberate to avoid accidentally over-permitting endpoints.
## Suggested Next Steps (Lightweight):
>- Remove legacy `/actuator/*` permit rules (tighten surface area) now that `/api/actuator` is stable.
>- Add `/api/actuator/info` test if you intend to rely on it for environment diagnostics.
>- Consider a simple uptime `JSON` at `/health` delegating to actuator for front-end simplicity (optional).
>- Add rate limiting or abuse monitoring around auth endpoints (future security baseline).
>- Evaluate whether to externalize allowed `CORS` origins into a single `ENV` list and document required format (to prevent future comma-splitting mishaps).
>- Add a dependency check or vulnerability scan step (if not already in CI).