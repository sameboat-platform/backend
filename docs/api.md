# SameBoat API Reference (Week 2 Baseline)

Version: Week 2 (2025-09-27)
Status: Auth stub + user self-profile

---
## 1. Overview
This document defines the current backend HTTP API surface for the Week 2 milestone. Scope is intentionally minimal: health endpoints, authentication stub (email + static password), session cookie issuance, and self-profile read/update.

Future (Week 3+) will extend with registration, password hashing, JWT or session rotation, audit logs, and possibly role-based endpoints.

---
## 2. Auth Model (Week 2 Stub)
- Login with any email + password `dev` at `POST /auth/login`.
- Server issues opaque session token stored in Postgres (planned) or in-memory (temporary) and sets cookie:
  - `SBSESSION=<random-token>; HttpOnly; SameSite=Lax` (Secure flag may be added outside dev).
- Subsequent authenticated requests rely on this cookie.
- Logout invalidates the session and clears the cookie.
- No registration, password complexity, or lockouts yet.

---
## 3. Error Format (Planned Standard)
All error responses SHOULD adopt (some endpoints may still return Spring defaults until hardened):
```json
{ "error": "UPPER_SNAKE_CODE", "message": "Human readable explanation" }
```
Example:
```json
{ "error": "UNAUTHORIZED", "message": "Session missing or expired" }
```

Common codes (planned):
| error | Meaning |
|-------|---------|
| UNAUTHORIZED | No valid session cookie provided |
| SESSION_EXPIRED | Session token exists but is expired |
| VALIDATION_ERROR | Request body failed validation rules |
| NOT_FOUND | Resource not found (future) |

---
## 4. Content & Headers
| Aspect | Value |
|--------|-------|
| Request/Response body | JSON UTF-8 |
| Auth (Week 2) | HttpOnly cookie `SBSESSION` |
| Versioning | Not yet (future: `/api/v1/`) |
| CORS | Configured in backend (see `CorsConfig`) |

---
## 5. Endpoints Summary
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /health | Public | Simple custom health check (`{"status":"ok"}`) |
| GET | /actuator/health | Public | Spring Boot detailed health indicator |
| POST | /auth/login | Public | Authenticate (stub) & set session cookie |
| POST | /auth/logout | Session | Invalidate current session |
| GET | /me | Session | Return current authenticated user profile |
| PATCH | /me | Session | Partial update of current user profile |

---
## 6. Data Models
### 6.1 UserDto
```json
{
  "id": "uuid",
  "email": "string",
  "displayName": "string",
  "avatarUrl": "string|null",
  "bio": "string|null",
  "timezone": "string|null",
  "role": "USER" // (future: ADMIN, MENTOR, etc.)
}
```

### 6.2 LoginRequest
```json
{ "email": "user@example.com", "password": "dev" }
```

### 6.3 LoginResponse
```json
{ "user": { /* UserDto */ } }
```

### 6.4 UpdateUserRequest (PATCH /me)
All fields optional; omitted fields are left unchanged.
```json
{
  "displayName": "New Name",
  "avatarUrl": "https://...",
  "bio": "Short bio...",
  "timezone": "America/Chicago"
}
```

Constraints (current or planned):
| Field | Rule |
|-------|------|
| displayName | 2–50 chars |
| bio | <= 500 chars |
| email | 3–254 chars (on creation) |

---
## 7. Endpoint Details

### 7.1 POST /auth/login
Authenticate user (stub). Creates session and sets cookie.

Request:
```http
POST /auth/login
Content-Type: application/json

{"email":"dev@example.com","password":"dev"}
```

Success (200):
Headers:
```
Set-Cookie: SBSESSION=<opaque>; HttpOnly; SameSite=Lax
```
Body:
```json
{ "user": { "id": "8f7d...", "email": "dev@example.com", "displayName": "Dev", "role": "USER" } }
```

Failure (401):
```json
{ "error": "UNAUTHORIZED", "message": "Invalid credentials" }
```

### 7.2 POST /auth/logout
Invalidate the active session.
```http
POST /auth/logout
Cookie: SBSESSION=<token>
```

Success (204 No Content) – MAY also return updated cookie clearing directive:
```
Set-Cookie: SBSESSION=deleted; Max-Age=0; HttpOnly; SameSite=Lax
```

If no valid session: 401 (same UNAUTHORIZED envelope).

### 7.3 GET /me
Fetch current user profile.

Request:
```http
GET /me
Cookie: SBSESSION=<token>
```

Success (200):
```json
{ "id": "8f7d...", "email": "dev@example.com", "displayName": "Dev", "avatarUrl": null, "bio": null, "timezone": null, "role": "USER" }
```

Unauthorized (401):
```json
{ "error": "UNAUTHORIZED", "message": "Session missing or expired" }
```

### 7.4 PATCH /me
Partial update of current user.

Request:
```http
PATCH /me
Content-Type: application/json
Cookie: SBSESSION=<token>

{"displayName":"My New Name","bio":"Short bio"}
```

Success (200): updated UserDto.

Validation failure (400):
```json
{ "error": "VALIDATION_ERROR", "message": "displayName length must be between 2 and 50" }
```

### 7.5 GET /health
Simple liveness endpoint.
```http
GET /health
```
200 OK:
```json
{"status":"ok"}
```

### 7.6 GET /actuator/health
Spring Boot health; format may vary.

---
## 8. Authentication Lifecycle (Stub)
1. Client calls `POST /auth/login`.
2. Backend sets cookie `SBSESSION`.
3. Client includes cookie automatically in subsequent requests (browser) or manually in API scripts.
4. Client may update profile via `PATCH /me`.
5. User logs out (`POST /auth/logout`) – backend deletes session and clears cookie.
6. Expired or invalid cookie returns 401.

---
## 9. Curl Examples
```bash
# Login
curl -i -c cookies.txt -H "Content-Type: application/json" \
  -d '{"email":"dev@example.com","password":"dev"}' \
  http://localhost:8080/auth/login

# Get current user
curl -b cookies.txt http://localhost:8080/me

# Update profile
curl -i -b cookies.txt -H "Content-Type: application/json" \
  -X PATCH -d '{"displayName":"NewName"}' http://localhost:8080/me

# Logout
curl -i -b cookies.txt -X POST http://localhost:8080/auth/logout
```

---
## 10. Postman / Environment Setup (Optional)
Suggested variables:
| Key | Value |
|-----|-------|
| baseUrl | http://localhost:8080 |

Save the `Set-Cookie` value after login; most HTTP clients handle it automatically if cookie jar is enabled.

---
## 11. Non-Functional Notes
| Concern | Current State | Future Direction |
|---------|---------------|------------------|
| Rate limiting | None | Add basic per-IP or per-email limit for login |
| Auditing | None | Add audit log (login/logout, profile change) |
| Password security | Plain stub | Hash + salted storage + registration flow |
| Token model | Opaque server session | Consider JWT access + refresh rotation |
| Error structure | Partially standardized | Enforce global exception handler |

---
## 12. Change Log
| Date | Change |
|------|--------|
| 2025-09-27 | Initial Week 2 API reference created |

---
## 13. Glossary
| Term | Definition |
|------|------------|
| Opaque token | Random value with no embedded claims; validated via lookup |
| UserDto | Public representation of a user sans sensitive fields |
| Session | Server-side record tying token to user & expiry |

---
## 14. Future Endpoints (Planned / Not Implemented Yet)
| Method | Path | Notes |
|--------|------|-------|
| POST | /auth/register | Create account (Week 3+) |
| POST | /auth/refresh | JWT or rotated session strategy |
| GET | /users/{id} | Restricted (self or admin) |
| GET | /stories | Domain content listing |
| POST | /stories | Create story (with validation) |
| POST | /auth/forgot-password | Password reset flow |

---
_This document will evolve as authentication and domain features expand._
