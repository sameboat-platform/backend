# SameBoat API Reference (Auth & User Profile Slice)

Status: Week 3 backend hardening complete (password complexity, rate limiting, session pruning). This document covers implemented endpoints and response contracts.

## Conventions
- All responses (success or error) are JSON.
- Authentication: opaque session cookie (primary name `SBSESSION`, legacy/alias accepted: `sb_session`) containing a UUID.
- Dev default TTL: 7 days. Prod profile sets `Secure` cookie, domain `.sameboatplatform.org`, TTL 14 days.
- Error envelope format:
  ```json
  { "error": "<CODE>", "message": "Human readable explanation" }
  ```
- Bio max length is 500 characters (intentional spec choice).

## Error Codes
| Code | Meaning | Typical Source |
|------|---------|----------------|
| UNAUTHENTICATED | No / invalid / garbage cookie | EntryPoint / controllers |
| BAD_CREDENTIALS | Bad email or password on login | /auth or /api/auth login |
| SESSION_EXPIRED | Cookie valid but session expired | Filter -> EntryPoint |
| EMAIL_EXISTS | Registration attempt with existing email (409) | /auth/register |
| VALIDATION_ERROR | Body validation failure (400) | Controllers |
| BAD_REQUEST | Explicit IllegalArgument (service) | Services / controllers |
| RATE_LIMITED | Too many requests (e.g., repeated failed logins) (429) | /auth/login |
| INTERNAL_ERROR | Unhandled exception (500) | Global handler |

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
Current `error` codes now include: `UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`, `VALIDATION_ERROR`, `BAD_REQUEST`, `RATE_LIMITED`, `INTERNAL_ERROR`.

## Authentication
### POST /auth/login (also `/api/auth/login`)
Authenticate with email/password. (Dev auto-create optional under test/profile if enabled.)

Request:
```json
{ "email": "user@example.com", "password": "********" }
```
Success (200):
```json
{ "user": { "id": "...", "email": "user@example.com", "displayName": "user@example.com", "avatarUrl": null, "bio": null, "timezone": null, "role": "USER" } }
```
Failure (401 BAD_CREDENTIALS):
```json
{ "error": "BAD_CREDENTIALS", "message": "Email or password is incorrect" }
```
Failure (429 RATE_LIMITED):
```json
{ "error": "RATE_LIMITED", "message": "Too many attempts; try again later" }
```

### POST /auth/register (also `/api/auth/register`)
Registers a new user (email must be unique). Returns a session cookie and minimal body containing the userId.

Password policy: minimum 8 characters and must include at least one uppercase, one lowercase, and one digit.

Request:
```json
{
  "email": "dev@example.com",
  "password": "Abcdef12",
  "displayName": "Dev"
}
```
Responses:
- 200:
  ```json
  { "userId": "<uuid>" }
  ```
- 409 EMAIL_EXISTS:
  ```json
  { "error": "EMAIL_EXISTS", "message": "Email already registered" }
  ```
- 400 VALIDATION_ERROR (e.g., password too weak)
  ```json
  { "error": "VALIDATION_ERROR", "message": "password must be at least 8 characters and include upper, lower, and digit" }
  ```

### POST /auth/logout (also `/api/auth/logout`)
Invalidates current session and clears cookie.

Responses:
- 204 No Content (idempotent)

## Current User
### GET /me (also `/api/me`)
Returns authenticated user.

401 cases:
- Missing/garbage cookie → `UNAUTHENTICATED`
- Expired cookie → `SESSION_EXPIRED`

### PATCH /me (also `/api/me`)
Partial profile update (requires at least one field; empty body rejected).

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

## Public Utility
### GET /api/version
Returns the deployed backend version. Public and unauthenticated.

Success (200):
```json
{ "version": "0.1.0" }
```

Notes:
- In production, the value reflects the JAR manifest when available; falls back to `project.version` during development/test.

## Error Handling Summary
| Scenario | Status | error code | Notes |
|----------|--------|------------|-------|
| Missing token / invalid UUID | 401 | UNAUTHENTICATED | No valid session cookie |
| Expired session | 401 | SESSION_EXPIRED | Session `expiresAt` passed |
| Bad credentials | 401 | BAD_CREDENTIALS | Login only |
| Registration duplicate | 409 | EMAIL_EXISTS | Email normalized & already present |
| Validation failure | 400 | VALIDATION_ERROR | Field constraints |
| Empty PATCH body | 400 | VALIDATION_ERROR | Enforced explicitly |
| Rate limited login attempts | 429 | RATE_LIMITED | 5 failures within 5 minutes |
| Generic uncaught exception | 500 | INTERNAL_ERROR | Trace id logged server-side |
| Illegal argument (service) | 400 | BAD_REQUEST | Future usage |

## Session Lifecycle
- Creation: at login or register (UUID as token value).
- Validation: filter loads session by UUID; sets request attribute for expired → code `SESSION_EXPIRED`; missing/invalid → `UNAUTHENTICATED`.
- Touch: `lastSeenAt` updated on authenticated requests.
- Expiry: 7 days dev / 14 days prod.
- Pruning: scheduled hourly job deletes expired sessions (server-side cleanup; expiry also enforced at request time).

## Example cURL Commands
Login:
```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"me@example.com","password":"Abcdef12"}'
```
Register:
```bash
curl -i -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"me2@example.com","password":"Abcdef12","displayName":"Me Two"}'
```
Expired (simulate): manually adjust DB row `expires_at` earlier and call `/me` with cookie to observe `SESSION_EXPIRED`.

---
For broader architecture overview see `docs/Architecture.md`.
