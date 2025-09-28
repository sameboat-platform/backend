## SameBoat — Domain & Hosting Checklist (Week 2)

>Goal: ship a real domain with CDN’d frontend and a stable API host.
>Stack: React (Vite) on Netlify, Spring Boot API on Render, Postgres on Neon, DNS via Cloudflare.

### Decisions (at a glance)
- Registrar & DNS: Cloudflare Registrar + Cloudflare DNS
- Domain: sameboat.<tld> (recommend .dev or .app; or .org if community vibe)
- Frontend: app.sameboat.<tld> → Netlify
- Backend API: api.sameboat.<tld> → Render
- DB: Neon (unchanged)

### CURRENT IMPLEMENTATION SNAPSHOT (Week 2 end)
| Area | Status | Notes |
|------|--------|-------|
| Domain purchased | Pending | No domain configured in repo yet |
| DNS zone created (Cloudflare) | Pending | All references still placeholder `<tld>` |
| Backend prod profile | Ready | `application-prod.yml` with secure cookie & CORS |
| CORS origins config | Ready | Uses `sameboat.cors.allowed-origins` list (dev + placeholders) |
| Session cookie name | Implemented | Primary `SBSESSION`, alias `sb_session` accepted at runtime |
| Cookie domain logic | Ready | Configurable via `sameboat.cookie.domain` (prod profile sets `.sameboat.<tld>`) |
| Secure flag (prod) | Ready | `sameboat.cookie.secure=true` in prod profile |
| TTL (prod) | Ready | 14 days (`sameboat.session.ttl-days=14`) |
| Registration & login | Implemented | Sets `SBSESSION` HttpOnly cookie |
| Logout clearing cookie | Implemented | Max-Age=0 consistent with domain/secure settings |
| Observability logs | Implemented | INFO login/logout, WARN expired/invalid session |
| Frontend deploy integration | Pending | No Netlify domain wiring yet |
| API host deployment | Pending | Render setup not documented in repo scripts |
| OpenAPI exposure | Pending | No `/v3/api-docs` yet |
| HSTS | Deferred | Add after domain + HTTPS stable |

### Prereqs
- Pick <tld> and buy sameboat.<tld> on Cloudflare Registrar.
- Frontend repo ready (Vite). Backend repo builds a runnable JAR (./mvnw package) or Docker image.

### 1) Cloudflare DNS (zone for sameboat.<tld>)
- [ ] Add CNAMEs
  -  app → <your-site>.netlify.app (Proxied ✅)
  -  api → <your-service>.onrender.com (Proxied ✅)
- [ ] Optional: www → app.sameboat.<tld> (Proxied ✅)
- [ ] Redirect apex to app (recommended)
  Use Cloudflare Rules → Redirects → “Bulk Redirects” or Page Rules:
  - https://sameboat.<tld>/* → https://app.sameboat.<tld>/$1 (301)

>Tip: Keep proxy (orange cloud) on for TLS + CDN + HSTS later.

---
### 2) Netlify (frontend)

- Connect repo (frontend-sameboat) in Netlify; set:
  -  Build command: pnpm install && pnpm build (or npm ci && npm run build)
  -  Publish directory: dist
- SPA redirects
Create public/_redirects (or in project root) with:

```bash
/*  /index.html  200
```
- Custom domain: add app.sameboat.<tld> → verify DNS → enable HTTPS (Let’s Encrypt).
- (Optional) Deploy Previews: keep enabled for PRs.

---

### 3) Render (backend)

#### Option A — Docker (recommended if you already have a Dockerfile)
- Create Web Service → “Blueprint/Docker” → point to backend repo.
- Auto-detect Dockerfile; set region close to Neon.

#### Option B — Native build (no Dockerfile)
- Create Web Service → connect backend repo.
- Build command:

  `./mvnw -DskipTests -ntp package`
- Start command:

  `java -Dserver.port=$PORT -jar target/*-SNAPSHOT.jar`

  _(Render provides `$PORT`;` -Dserver.port=$PORT` ensures Spring listens correctly.)_

#### Env vars (Render → Environment):
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>:5432/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<pass>
SAMEBOAT_COOKIE_DOMAIN=.sameboat.<tld>
SAMEBOAT_COOKIE_SECURE=true
SAMEBOAT_SESSION_TTL_DAYS=14
SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboat.<tld>,https://*.netlify.app
```
_(Spring relaxed binding maps environment uppercase underscore vars to sameboat.*)_

#### Custom domain (Render)
- Add api.sameboat.<tld> → complete DNS CNAME → enable HTTPS.

### 4) Backend CORS & Cookie (prod)

Current implementation (type-safe properties) already covers this; snippet illustrative only:
```java
// In production, properties drive these values:
props.getCors().getAllowedOrigins(); // includes app.sameboat.<tld>, *.netlify.app
props.getCookie().isSecure();        // true
props.getCookie().getDomain();       // .sameboat.<tld>
props.getSession().getTtlDays();     // 14
```
Login cookie issuance uses `SBSESSION`; alias `sb_session` still accepted inbound.

---
### 5) Frontend dev settings (unchanged)

- [ ] `frontend-sameboat/.env.local`:
```ini
VITE_API_BASE=/api
```
- [ ] `vite.config.ts` dev proxy:
```ts
server: {
  proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
}
```

---
### 6) Verification checklist

#### DNS/TLS
- [ ] `https://app.sameboat.<tld>` loads Netlify site (valid cert).
- [ ] `https://api.sameboat.<tld>/actuator/health` returns {"status":"UP"} (valid cert).
- [ ] Apex https://sameboat.<tld> redirects to https://app.sameboat.<tld>.

#### CORS & Cookies
- [ ] From `app.sameboat.<tld>`, login sets `SBSESSION` (Secure, HttpOnly, SameSite=Lax, Domain=.sameboat.<tld>).  
- [ ] XHR to `https://api.sameboat.<tld>/api/me` includes cookie → 200.
- [ ] Expired session returns 401 JSON `{ "error": "SESSION_EXPIRED" }`.
- [ ] Garbage cookie returns 401 JSON `{ "error": "UNAUTHENTICATED" }`.

#### Netlify Previews
- [ ] A Deploy Preview (e.g., `https://deploy-preview-123--<site>.netlify.app`) can call API (CORS allow list includes `*.netlify.app`).

#### Backend logs
- [ ] INFO lines on login/logout; WARN on invalid/expired session visible in Render logs.

---
### 7) Nice-to-haves (later)
- [ ] Enable HSTS in Cloudflare (after HTTPS confirmed) & set min TLS 1.2.
- [ ] Add OpenAPI (`springdoc-openapi`) to serve `/v3/api-docs`; generate TS types in FE.
- [ ] WAF / rate limiting rule (basic bot mitigation) in Cloudflare.
- [ ] Structured JSON logging (trace correlation) for auth events.
- [ ] Observability: add `/actuator/metrics` + remote scraping.

---
### 8) Rollback plan (just in case)
- [ ] Keep the old URLs (default Netlify & Render) live until new ones are verified.
- [ ] If issues arise, revert DNS CNAMEs to provider defaults; TTL low (300s) to speed rollback.

---
### 9) Post‑Week 2 Action Items
| Priority | Action | Rationale |
|----------|--------|-----------|
| High | Acquire domain & provision Cloudflare zone | Unblocks all prod validation |
| High | Deploy backend to Render with prod profile | Validate secure cookie + CORS in real env |
| High | Deploy frontend to Netlify with API base pointing to Render | End‑to‑end session test |
| Medium | Add migration test to main CI (Testcontainers) | Prevent drift & enforce schema early |
| Medium | Add rate limiting / basic abuse guard | Mitigate brute force on login |
| Medium | OpenAPI generation + publish docs | Improve FE contract stability |
| Low | Session pruning job | Control table growth |
| Low | Add HSTS + security headers | Harden prod edge |

### 10) Quick Validation Script (once deployed)
```bash
# Health
curl -i https://api.sameboat.<tld>/actuator/health

# Register user
curl -i -X POST https://api.sameboat.<tld>/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"smoke@sameboat.<tld>","password":"Abcdef1!"}'

# Extract cookie then call /me
COOKIE=$(curl -i -s https://api.sameboat.<tld>/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"smoke@sameboat.<tld>","password":"Abcdef1!"}' | grep -Fi Set-Cookie | sed 's/;.*//')

curl -i https://api.sameboat.<tld>/me -H "Cookie: $COOKIE"
```

---
_For broader architecture overview see `docs/Architecture.md`._