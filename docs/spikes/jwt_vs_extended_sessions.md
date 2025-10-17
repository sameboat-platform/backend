# Spike: JWT vs. Extended Opaque Sessions (Decision Record)

Time-box: <= 4 hours (research + write-up). This document captures the tradeoffs, a concise decision matrix, and a pragmatic migration plan.

Related: See also `docs/spikes/jwt-session-tradeoffs.md` for a deeper narrative.

## Context
Current auth uses opaque, DB-backed session tokens (UUID) stored in `sessions` with server-side lookup and scheduled pruning. We considered:
- A) JWT access tokens (stateless)
- B) Hybrid: short-lived JWT access token + server-side refresh/session
- C) Stay with extended opaque sessions (current), adding rotation/caching as needed

## Decision (for MVP)
Choose C: DB-backed opaque sessions. Rationale: simple revocation, predictable behavior, lower implementation risk under SECURITY_BASELINE. Revisit B when scaling beyond a single backend instance or introducing additional services.

## Decision Matrix
Legend: 1 = poor, 3 = neutral, 5 = strong. Weights (W) emphasize MVP priorities (revocation, simplicity, security).

| Criteria                         | W | A) JWT | B) Hybrid | C) Opaque |
|----------------------------------|---|-------:|----------:|----------:|
| Simplicity (impl & ops)          | 3 |      2 |         2 |         5 |
| Revocation control               | 5 |      2 |         4 |         5 |
| Horizontal scaling readiness     | 3 |      5 |         5 |         3 |
| Request latency / DB load       | 2 |      5 |         4 |         2 |
| Security risk surface (claims)   | 3 |      3 |         3 |         4 |
| Extensibility (future services)  | 2 |      4 |         5 |         3 |

Weighted score (approx.):
- A) JWT ≈ 2*3 + 5*2 + 3*5 + 2*5 + 3*3 + 2*4 = 6 + 10 + 15 + 10 + 9 + 8 = 58
- B) Hybrid ≈ 2*3 + 5*4 + 3*5 + 2*4 + 3*3 + 2*5 = 6 + 20 + 15 + 8 + 9 + 10 = 68
- C) Opaque ≈ 5*3 + 5*5 + 3*3 + 2*2 + 4*3 + 3*2 = 15 + 25 + 9 + 4 + 12 + 6 = 71

Conclusion: Opaque scores slightly higher for MVP due to revocation and simplicity.

## Pros/Cons (condensed)
- A) JWT
  - Pros: no per-request DB lookup, naturally cross-service, good for microservices
  - Cons: revocation hard (denylist or key rotation), risk of overstuffed claims, CSRF/CORS considerations if cookie-based
- B) Hybrid
  - Pros: JWT speed for most calls; server-side refresh enables revocation/rotation
  - Cons: two-token complexity, rotation logic, more moving parts
- C) Opaque sessions (chosen)
  - Pros: simple revocation (delete row), least surprise, minimal token semantics exposure
  - Cons: DB hit per request (cacheable), needs shared store for scale-out

## Migration Plan
1. Short term (MVP)
   - Keep opaque sessions; ensure scheduled pruning (hourly) and expiry checks on every request
   - Add optional in-memory read-through cache (e.g., Caffeine) to cut DB reads; DB remains source of truth
   - Add session rotation on sensitive events or idle thresholds to reduce replay windows
2. Scale-out readiness
   - Externalize sessions + rate limits to Redis when adding multiple instances; keep same API contracts
   - Introduce structured logging + metrics to observe hot paths
3. Hybrid/JWT revisit (post-MVP)
   - Prototype Hybrid (short-lived JWT + server refresh) with minimal claims (sub, iat, exp, jti)
   - Add key rotation plan (JWKS, rollover cadence) and refresh rotation rules

## Operational Notes
- Cookies: HttpOnly always; `Secure` in prod; evaluate `SameSite=Strict` for auth endpoints
- CORS: strict allowlist only (`https://app.sameboatplatform.org`), credentials enabled
- Logging: never log tokens; info logs on rate-limit triggers

## References
- Detailed narrative: `docs/spikes/jwt-session-tradeoffs.md`
- Architecture overview: `docs/Architecture.md`
- API reference: `docs/api.md`
