## Week 2 Mentor Overview

### Huge week. You crossed the “toy → real app” line:
- Live path end-to-end: Netlify frontend → Render backend → Neon DB. Health check is green in prod.
- Ops hardening: multi-stage Dockerfile, non-root, `$PORT` support, prod profiles, explicit CORS, secure cookie policy, actuator base path.
- Quality: backend tests all green, CI fixed, frontend lint/type clean; docs + contributor guidance are solid.
- Governance: repo rules, Dependabot, and CI gates are in place. Nice.

Nothing in the agents’ summaries worries me—changes were incremental, security-minded (permit only what’s needed), and backed by tests + docs. That’s exactly how to scale.

### Fast follow checklist (short + concrete):
1. #### Domains
   - When `sameboatplatform.com` finishes:
     - Netlify → add `app.sameboatplatform`.com, set one primary (either .org or .com), enable “enforce primary”.
     - Render → add `api.sameboatplatform.com` (TLS), `keep api-sameboat.onrender.com` as fallback.
     - Update CORS allowed origins to include the chosen primary (and temp the other until you flip).
     - Cookie domain: set to `.sameboatplatform.com` (or `.org`) to match your canonical choice.

2. #### Env/Secrets (Render)
   - `SPRING_PROFILES_ACTIVE=prod`
   - `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` (Neon)
   - `SAMEBOAT_CORS_ALLOWED_ORIGINS` (newline or comma list)
   - `SAMEBOAT_COOKIE_DOMAIN`, `SAMEBOAT_COOKIE_SECURE=true`, `SAMEBOAT_SESSION_TTL_DAYS=14`

3. #### Frontend
   - Keep `VITE_API_BASE_URL` pointing to Render until custom `api`. is live; then flip.
   - Add Vitest + a tiny test (the `health.ts` guard). Good ROI.

4. #### CI/Rules hygiene
   - Require the single canonical check in each repo (the job name that actually appears).
   - Allow Dependabot auto-merge only when CI passes and compat ≥80% (review <80% or “unknown”).

5. #### Observability
   - Add UptimeRobot/BetterStack to ping `/api/actuator/health`.
   - Netlify Deploy notifications to the repo.

6. #### Security nits (soon)
   - Consider `SameSite=Lax` on the session cookie; add simple rate-limit on `/auth/*` later.

### Risks to watch
   - Cookie/CORS mismatch when you switch from `.org` to `.com`. Pick one canonical domain and align CORS + cookie domain + frontend base URL together in a single PR.
   - Wildcard origins—you avoided them (good). Keep it that way.

Overall: you shipped a deployable, test-backed, documented slice of the platform in a week. That’s A-level progress. 🎉