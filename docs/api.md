# SameBoat API Reference (Auth & User Profile Slice)

Status: Week 2 vertical slice (dev stub for auth password). This document covers implemented endpoints and response contracts.

## Conventions
- All responses (success or error) are JSON.
- Authentication: opaque session cookie `SBSESSION=<UUID>` (HttpOnly, SameSite=Lax, 7-day TTL) returned by login.
- Unauthorized / validation failures return a uniform error envelope:
  ```json
  { "error": "UNAUTHORIZED", "message": "Authentication required" }
  ```
  or
  ```json
  { "error": "VALIDATION_ERROR", "message": "displayName size must be between 2 and 50" }
  ```
- Expired session returns:
  ```json
  { "error": "SESSION_EXPIRED", "message": "Session expired" }
  ```
- Bio max length is 500 characters (intentional spec choice for Week 2).

## Data Models
### UserDto
```json
{
  "id": "<uuid>",
  "email": "user@example.com",
  "displayName": "user@example.com",
  "avatarUrl": null,
  "bio": null,
  "timezone": null,
  "role": "USER"
}
```

### ErrorResponse
```json
{ "error": "<CODE>", "message": "Human readable explanation" }
```
Current `error` codes: `UNAUTHORIZED`, `SESSION_EXPIRED`, `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR`.

## Authentication
### POST /auth/login
Authenticate (stubbed: password must equal `dev`). Creates a session row and issues `SBSESSION` cookie.

Request:
```json
{ "email": "user@example.com", "password": "dev" }
```
Success (200):
```json
{ "user": { "id": "...", "email": "user@example.com", "displayName": "user@example.com", "avatarUrl": null, "bio": null, "timezone": null, "role": "USER" } }
```
Set-Cookie header example:
```
SBSESSION=2c7b8c04-...; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax
```
Failure (401):
```json
{ "error": "UNAUTHORIZED", "message": "Invalid credentials" }
```

### POST /auth/logout
Invalidates current session (if present) and expires cookie.

Request: no body.
Responses:
- 204 No Content (always; idempotent)

## Current User
### GET /me
Returns the authenticated user.

Success (200): `UserDto`

401 cases:
- Missing/invalid token: `UNAUTHORIZED`
- Expired token: `SESSION_EXPIRED`

### PATCH /me
Partially update profile fields. Empty JSON `{}` is rejected with `VALIDATION_ERROR`.

Request body (all shown fields optional – only included keys are changed):
```json
{
  "displayName": "New Name",
  "avatarUrl": "https://cdn.example.com/avatar.png",
  "bio": "Short bio (<=500 chars)",
  "timezone": "America/Chicago"
}
```
Validation rules:
- `displayName`: 2..50 chars
- `avatarUrl`: <=255 chars
- `bio`: <=500 chars
- `timezone`: <=100 chars (placeholder; not yet validated against IANA list)
- Non-empty update: at least one field must be provided

Responses:
- 200 Updated `UserDto`
- 400 Validation error → `VALIDATION_ERROR`
- 401 If not authenticated / expired (distinct codes as above)

## Error Handling Summary
| Scenario | Status | error code | Notes |
|----------|--------|------------|-------|
| Missing token / not logged in | 401 | UNAUTHORIZED | No SBSESSION cookie or invalid UUID |
| Expired session | 401 | SESSION_EXPIRED | Filter detects expired session timestamp |
| Bad credentials | 401 | UNAUTHORIZED | Login only |
| Validation failure | 400 | VALIDATION_ERROR | Aggregated field messages joined by comma |
| Empty PATCH body | 400 | VALIDATION_ERROR | Message: At least one field must be provided |
| Generic uncaught exception | 500 | INTERNAL_ERROR | Message intentionally generic |
| Illegal argument in service | 400 | BAD_REQUEST | Future usage |

## Session Lifecycle
- Creation: at login (UUID primary key = opaque token value).
- Validation: filter loads session by UUID; rejects if `expiresAt <= now` (SESSION_EXPIRED) or missing.
- Touch: lastSeenAt updated per request.
- Expiry: fixed 7 days (future: sliding window / rotation).

## Example cURL Commands
Login:
```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"me@example.com","password":"dev"}'
```
Use returned cookie:
```bash
curl -i http://localhost:8080/me \
  -H "Cookie: SBSESSION=<uuid-from-login>"
```
Update profile:
```bash
curl -i -X PATCH http://localhost:8080/me \
  -H "Content-Type: application/json" \
  -H "Cookie: SBSESSION=<uuid>" \
  -d '{"displayName":"New Display","bio":"Short bio"}'
```
Logout:
```bash
curl -i -X POST http://localhost:8080/auth/logout \
  -H "Cookie: SBSESSION=<uuid>"
```

## Roadmap Notes (Beyond Week 2)
| Planned | Description |
|---------|-------------|
| Real password hashing | Replace stub `dev` with BCrypt + registration flow |
| Session hardening | Secure & SameSite=None in HTTPS env, rotation, IP / UA binding |
| Role expansion | Admin / Moderator checks on endpoints |
| Refresh & Idle timeout | Sliding session extension + shorter idle expiry |
| OpenAPI merge | Incorporate these endpoints into `openapi/sameboat.yaml` |

---
For broader architecture overview see `docs/Architecture.md`.
