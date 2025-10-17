# Known Risks and Mitigations

This document lists current risks in the backend and how we plan to mitigate them. It’s a living file; update as features evolve.

## Authentication & Session Management
- In-memory rate limiting
  - Risk: Rate limiter state is reset on application restart (risk of brute force during a restart) and can produce false positives behind NAT/shared IPs.
  - Mitigation: Threshold set conservatively (5 attempts/5 min). Consider moving to a distributed store (Redis) with IP + device fingerprinting for multi-instance environments. Add allowlist for known CI test users/domains.

- Session pruning schedule
  - Risk: If pruning job lags or fails, expired sessions could persist longer than intended, slightly increasing DB footprint.
  - Mitigation: Pruner runs hourly with transactional bulk delete. Expiry is also enforced at request time, so stale rows don’t grant access.

- Cookie security
  - Risk: Misconfiguration of cookie domain/flags can expose sessions to subdomains or non-TLS contexts.
  - Mitigation: Properties-driven; `Secure` and proper domain only in `prod`. CORS is strict to the SPA origin.

## Input Validation & Error Handling
- Password complexity
  - Risk: Too lax passwords increase account takeover risk.
  - Mitigation: Enforced via Bean Validation (min 8, includes upper/lower/digit). Consider adding symbol requirement and breached password checks in future.

- Error envelope
  - Risk: Overly detailed errors leak information.
  - Mitigation: Centralized error envelope; `BAD_CREDENTIALS` is generic. Continue to avoid echoing sensitive inputs.

## Data & Migrations
- Flyway immutability
  - Risk: Editing applied migrations causes drift.
  - Mitigation: Guard scripts + CI gate. Always add new migrations.

- Test vs. Prod DB behavior
  - Risk: H2 and Postgres have subtle differences.
  - Mitigation: Keep JPQL portable; use Testcontainers profile for schema verification.

## Operational
- Single-node assumptions
  - Risk: In-memory components (rate limiter) don’t scale horizontally.
  - Mitigation: Plan Redis-backed rate limiting and session store scale-out when moving to multi-instance.

- Logging
  - Risk: Over-logging auth events could create noise or risk PII.
  - Mitigation: Minimal INFO logs; avoid payloads; consider structured logging in prod profile.

---
Related: docs/spikes/jwt-session-tradeoffs.md for token strategy considerations.

