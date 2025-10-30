# Week 4 – Backend Plan (Checklist)

Context: Backend-only items extracted from the Week 4 plan. Status as of 2025-10-29.

## Done
- [x] Environment onboarding: `.env.example` present at repo root and referenced in README (copy to `.env` for local runs).
- [x] CI pipeline (BACKEND_CI_GUARD): `.github/workflows/backend-ci.yml` runs migration immutability check, `mvn verify` (unit + integration tests), JaCoCo coverage gate (≥ 70%), and a Flyway schema test profile step.
- [x] Auth hardening: password complexity validation, in-memory login rate limiting returning `RATE_LIMITED` on abuse, and scheduled session pruning job.
- [x] Public endpoints: `GET /health`, `GET /actuator/health`, and `GET /api/version` implemented and covered by integration tests.
- [x] Profiles & CORS: `dev` vs `prod` profiles with secure cookie/domains in prod; CORS allowlist configured (credentials enabled, no wildcards).
- [x] Flyway migrations: schema aligned with `UserEntity` (e.g., timezone column) via a new migration; historical migrations protected by immutability scripts.
- [x] NOT_FOUND mapping: Added `ResourceNotFoundException`, mapped to 404 in `GlobalExceptionHandler`, service method `UserService.getByIdOrThrow(UUID)`, gated `GET /users/{id}` uses it when enabled; tests added (service + WebMvc 404 envelope).
- [x] BAD_REQUEST mapping test: Focused WebMvc test asserting `IllegalArgumentException` → `{ "error": "BAD_REQUEST" }`.
- [x] OpenAPI sync: `openapi/sameboat.yaml` updated to include `/api/version` and `ErrorResponse` schema with current error codes; brief Week 4 backend summary added to `docs/Architecture.md`.
- [x] Coverage gate: verified JaCoCo ≥ 70% (current ~84%).

## Left (Backend-focused)
- [ ] None for Week 4. Carryovers: raise coverage target to 75% in a future cycle; enable gated `/users/{id}` when admin/public profile work lands.

## Scope Notes
- Excluded (frontend-only): health polling backoff/pause, reduced-motion accessibility controls, debug panel utilities, identicon fallback, and visibility-driven session refresh.
