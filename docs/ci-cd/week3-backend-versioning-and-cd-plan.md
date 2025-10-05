# Week 3 – Backend Versioning + Continuous Delivery (CD) Plan

Applies BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.

What we’re doing
- Adopt clear artifact versioning in pom.xml (SemVer).
- Keep /api endpoints stable; no breaking changes in Week 3.
- Run Continuous Delivery: merge to main auto-deploys to Render once checks pass.
- Keep Flyway migrations immutable and coverage ≥ 70% (Jacoco).

How versioning works here
- Artifact version (backend JAR) in pom.xml, using SemVer X.Y.Z.
  - Suggested now: 0.1.0. End of Week 3: bump to 0.2.0 if we ship user-visible changes.
  - Tag releases in Git: v0.1.0, v0.2.0; add brief release notes.
- API version (HTTP): remain on /api without explicit /v1 for now. Avoid breaking contract. Introduce /api/v1 only when needed.

Branching and releases
- Short-lived feature branches (feat/<desc>) → PR → main.
- Protected main: require build + tests + coverage.
- After merge, tag a release if desired (vX.Y.Z) and let Render auto-deploy from main.

Environments & profiles
- Dev (SPRING_PROFILES_ACTIVE=dev)
  - CORS: http://localhost:5173
  - Cookie: Secure=false; Domain omitted (host-only)
  - Use local Postgres or env-driven defaults per application.yml
- Prod (SPRING_PROFILES_ACTIVE=prod)
  - CORS: https://app.sameboatplatform.org (and explicitly listed prod domains)
  - Cookie: Secure=true; Domain=.sameboatplatform.org
  - Actuator base path: /api/actuator (public: /health, /info)

CI/CD notes (conceptual)
- Build & test: mvn -B -ntp clean verify (coverage gate enforced)
- Package: Boot JAR
- DB: Flyway at app startup (no editing old migrations)
- Deploy: Render auto-deploys on push to main
- Verify: /api/actuator/health & /api/actuator/info; login → /api/me round-trip with credentials: 'include'

Render env var policy (prod)
- Keep: SPRING_PROFILES_ACTIVE=prod, SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
- Remove (profile files own these): SAMEBOAT_COOKIE_DOMAIN, SAMEBOAT_COOKIE_SECURE, SAMEBOAT_CORS_ALLOWED_ORIGINS, SAMEBOAT_SESSION_TTL_DAYS

Stability guarantees (Week 3)
- Error envelope remains: { "error": "CODE", "message": "..." }
- Codes in use: BAD_CREDENTIALS, UNAUTHENTICATED, SESSION_EXPIRED, EMAIL_EXISTS, VALIDATION_ERROR
- /api/me requires auth; 401 with UNAUTHENTICATED/SESSION_EXPIRED when missing/expired

BACKEND_CI_GUARD reminder
- If .github/workflows/backend-ci.yml exists, modify only that file for CI changes.
- If it appears missing, confirm manually before generating a new workflow.
- We are not creating/modifying any workflows in this plan.

