# Architecture (MVP)

**FE (React+Vite) →** HTTP/JSON → **BE (Spring Boot 3, Java 21) →** JDBC → **Postgres (Neon)**

- Auth: opaque session cookie (UUID) with server-side lookup; registration enforces password complexity; login rate-limited; scheduled session pruning.
- Core entities: users, stories, trust_events
- Migrations: Flyway (V1 applied)
- Envs: DB_URL only (OpenAI later)

---
## Deployment & Hosting (Current Plan)
| Layer | Provider | Domain / Access | Notes |
|-------|----------|-----------------|-------|
| Backend API | Render (Web Service) | https://api.sameboatplatform.org | Starter tier acceptable for MVP; scale horizontally later. |
| Frontend (SPA) | Netlify | https://app.sameboatplatform.org | Calls API subdomain (CORS & cookies). |
| Root Domains | Registrar / DNS | sameboatplatform.org (+ .com redirect) | `.com` 301 → `.org`. Configure APEX + `www` as needed. |
| Database | Neon Postgres (managed) | TLS required (jdbc `sslmode=require`) | Branching for previews later. |
| Session Cookie | Browser (Set by API) | Domain: `.sameboatplatform.org` | Name `SBSESSION`, `Secure` (prod), `HttpOnly`, `SameSite=Lax`. |
| CORS Allowlist | Spring Config | https://app.sameboatplatform.org | Credentials (cookies) allowed; keep list minimal. |

### Environment Variables (Prod Example)
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org
SAMEBOAT_COOKIE_SECURE=true
SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboatplatform.org
```

### Domain Strategy
- Dedicated API subdomain isolates backend scaling / headers.
- Shared cookie domain `.sameboatplatform.org` enables session cookie for API while frontend served from `app.`.
- Redirect policy: all legacy root (.com) traffic → canonical `.org`.

### Security Considerations
- Enforce HTTPS at Render & Netlify; HTTP → HTTPS redirect.
- TLS enforced to Neon with `sslmode=require`.
- Only whitelisted origin gets credentialed CORS; avoid wildcard origins.
- Registration password policy: min 8 chars, includes upper/lower/digit.
- Rate limiting on login (5 attempts / 5 min) returns 429 RATE_LIMITED.
- Scheduled session pruning removes expired rows (hourly); expiry also enforced at request time.

### Future Enhancements
- Add staging environment: `staging-api.sameboatplatform.org` + Neon branch database.
- CDN caching layer for static assets (handled by Netlify). API remains dynamic.
- Potential WAF / distributed rate limiting (e.g., Redis) for multi-instance auth endpoints.

---

## Week 4 Backend Summary
- Introduced `ResourceNotFoundException` mapped to HTTP 404 with error code `NOT_FOUND` via `GlobalExceptionHandler`.
- Standardized controller 404 behavior by adding service method `UserService.getByIdOrThrow(UUID)` and using it in `GET /users/{id}`.
- Added focused tests: service throws `ResourceNotFoundException`; WebMvc test asserts 404 NOT_FOUND envelope; BAD_REQUEST mapping asserted for `IllegalArgumentException`.
- Synced OpenAPI (`openapi/sameboat.yaml`) to include `/api/version` and the error envelope schema with current error codes.
