## SameBoat — Domain & Hosting Checklist (Week 2)

>Goal: ship a real domain with CDN’d frontend and a stable API host.
>Stack: React (Vite) on Netlify, Spring Boot API on Render, Postgres on Neon, DNS via Cloudflare.

### Decisions (at a glance)
- Registrar & DNS: Cloudflare Registrar + Cloudflare DNS
- Domain: sameboat.<tld> (recommend .dev or .app; or .org if community vibe)
- Frontend: app.sameboat.<tld> → Netlify
- Backend API: api.sameboat.<tld> → Render
- DB: Neon (unchanged)

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
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>:5432/<db>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME=<user>`
- `SPRING_DATASOURCE_PASSWORD=<pass>`
- `SESSION_TTL_DAYS=14`
- `COOKIE_DOMAIN=.sameboat.<tld>`

#### Custom domain (Render)
- Add api.sameboat.<tld> → complete DNS (already CNAME’d) → enable HTTPS.

### 4) Backend CORS & Cookie (prod)

Allow your real frontend + deploy previews; set cookie for parent domain:
```text
// CORS bean (prod)
@Bean
CorsConfigurationSource corsConfigurationSource() {
var cfg = new CorsConfiguration();
cfg.setAllowedOrigins(List.of(
"https://app.sameboat.<tld>",
"https://*.netlify.app" // Netlify Deploy Previews
));
cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
cfg.setAllowedHeaders(List.of("*"));
cfg.setAllowCredentials(true);
var src = new UrlBasedCorsConfigurationSource();
src.registerCorsConfiguration("/**", cfg);
return src;
}
```
When issuing the session cookie on login:
```java
ResponseCookie cookie = ResponseCookie.from("sb_session", sessionId)
.httpOnly(true)
.secure(true)                 // prod
.sameSite("Lax")
.domain(".sameboat.<tld>")    // share across app/api subdomains
.path("/")
.maxAge(Duration.ofDays(14))
.build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

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
- [ ] From `app.sameboat.<tld>`, login sets sb_session; Secure; SameSite=Lax; Domain=.sameboat.<tld>.
- [ ] XHR to `https://api.sameboat.<tld>/api/me` includes cookie and returns 200.

#### Netlify Previews
- [ ] A Deploy Preview domain (e.g., `https://deploy-preview-123--<site>.netlify.app`) can call the API (CORS allow list includes `*.netlify.app`).

#### Backend logs
- [ ] INFO on login/logout; WARN on invalid/expired session (as implemented).

---

### 7) Nice-to-haves (later)
- [ ] Enable HSTS in Cloudflare (after you confirm HTTPS everywhere).
- [ ] Add OpenAPI (`springdoc-openapi`) to serve `/v3/api-docs`; generate TS types in FE.
- [ ] Add ruleset requiring Backend CI checks to pass (already done).
- [ ] Move repos to GitHub org; centralize secrets and rules.

---

### 8) Rollback plan (just in case)
- [ ] Keep the old URLs live until new ones are verified.
- [ ] If something breaks, point `app.` back to the Netlify default subdomain and `api`. back to Render’s `.onrender.com` origin (DNS CNAME changes are instant; allow up to a few minutes for global propagation).

---
_For broader architecture overview see `docs/Architecture.md`._