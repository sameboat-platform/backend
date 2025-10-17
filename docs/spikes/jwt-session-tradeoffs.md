# Spike: JWT (stateless) vs. Opaque Session Tokens

Goal: Document tradeoffs to inform future auth evolution (refresh tokens, rotation, multi-instance).

## Current Architecture (Baseline)
- Opaque session token (UUID) stored in DB table `sessions`.
- Cookie: `SBSESSION` (HttpOnly; `Secure` only in prod; `SameSite=Lax`).
- Validation: server-side lookup by UUID; expiry enforced on read; periodic pruning job.
- Pros: Simple revocation (delete row), easy server-side invalidation, minimal token parsing logic, no token leakage in logs.
- Cons: DB hit per request (can be mitigated via caching), requires shared store for horizontal scale.

## Option A: JWT Access Tokens (+ optional Refresh)
- Self-contained signed tokens; server validates signature and claims.
- Pros:
  - No DB lookup per request (lower latency at small/medium scale).
  - Easier to share across services without central session store.
- Cons:
  - Revocation is hard (must track denylist or rotate signing keys).
  - Risk of over-encoding PII/roles in token (SECURITY_BASELINE: keep minimal claims).
  - Clock skew/expiry edge cases; token replay if stolen until expiry.
  - Cookie vs. Authorization header tradeoffs; CSRF considerations if cookie-based.

## Option B: Hybrid (Short-lived JWT + Server Session/Refresh)
- JWT access token (short TTL) issued alongside an opaque refresh/session stored server-side.
- Pros:
  - Reduces DB load on most calls; refresh path allows revocation and rotation.
  - Better control over long-lived authentication state.
- Cons:
  - Complexity (two token classes, rotation logic, storage and sync).

## Security Considerations (All Options)
- Always HttpOnly cookies; `Secure` in production; evaluate `SameSite=Strict` vs `Lax`.
- Strict CORS allowlist (no wildcard). No tokens in logs.
- Enforce password complexity and consider breached-password checks.
- Rate limit login and token refresh endpoints (current in-memory limiter → Redis in future).

## Migration Path (Recommended)
1. Keep opaque sessions for MVP (current design). Optimize with an in-memory cache if needed (Caffeine) while retaining DB as source of truth.
2. Add session rotation on sensitive actions (reduce replay window).
3. If multi-instance scale is required: move sessions and rate limits to Redis.
4. Re-evaluate Hybrid JWT for services integration; keep minimal claims (subject, iat, exp, jti). Introduce key rotation plan.

## Decision (for now)
- Stay with opaque DB-backed sessions. Simplicity and revocability align with MVP needs and SECURITY_BASELINE. Revisit JWT hybrid when scaling beyond a single backend instance or adding external services.

