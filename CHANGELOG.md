# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
### Added
- (placeholder)

### Changed
- (placeholder)

### Fixed
- (placeholder)

---

## [v0.1.1] - 2025-10-29 – Week 4 backend wrap-up
### Added
- Password complexity validation on registration (min 8 chars, includes upper/lower/digit) with validation message.
- In-memory login rate limiting (5 attempts within 5 minutes by email+IP) returning HTTP 429 `RATE_LIMITED`.
- Scheduled session pruning job (hourly) with JPQL bulk delete; transactional execution.
- Integration tests: password complexity, rate limiting, session pruning.
- Documentation: `docs/RISKS.md` and `docs/spikes/jwt-session-tradeoffs.md`.
- Public version endpoint `GET /api/version` that returns the deployed version (from JAR manifest when available, falling back to `project.version`).
- Spring profiles for development and production:
  - `dev`: in-memory H2, relaxed cookies (host-only, `Secure=false`), and CORS including `http://localhost:5173`.
  - `prod`: Postgres/Neon, secure cookies (domain + `Secure=true`), explicit CORS allowlist.
- CORS configuration via `CorsConfig` and Security chain `.cors()`; credentials enabled and origins restricted to configured list.
- CI release job on tag push (`v*`): builds JAR and attaches it to a GitHub Release using `softprops/action-gh-release@v1`.
- `ResourceNotFoundException` mapped by `GlobalExceptionHandler` to HTTP 404 with error code `NOT_FOUND`; service methods use throw-or-404 semantics for true missing cases.
- Focused tests for error envelopes:
  - `NOT_FOUND` (404) mapping from service-thrown exception
  - `BAD_REQUEST` (400) mapping for `IllegalArgumentException`
- Gated user read endpoint `GET /users/{id}` returning `PublicUserDto` (no email); enabled only when `sameboat.endpoints.user-read=true`. Access allowed to self or `ADMIN`.
- OpenAPI spec updates: reusable `ErrorResponse` schema with current error codes; documented `/api/version` response.

### Changed
- `README.md`: Documented password policy, `RATE_LIMITED` error code, and updated sample passwords; linked to new docs.
- `docs/api.md`: Updated error codes, register/login behavior, and added pruning notes.
- `Architecture.md`: Reflected opaque sessions, rate limiting, password policy, and pruning.
- `CONTRIBUTING.md`: Included `RATE_LIMITED` in error codes and noted password complexity under security.
- Unified Actuator base path to `/actuator` across profiles; health and info remain exposed publicly via security rules. Legacy `/api/actuator/*` references were removed from tests/config where applicable.
- Security rules clarified: public `GET /health`, `GET /actuator/health`, `GET /actuator/info`, auth endpoints `POST /api/auth/login|register|logout` (and legacy `/auth/*`) remain public; other endpoints require authentication (e.g., `GET /api/me`).
- Input validation tightened on auth payloads (`@Valid` + Bean Validation). Validation errors are mapped by `GlobalExceptionHandler` to `400` with `{ "error":"VALIDATION_ERROR" }`.
- Documentation updates for profile usage, CORS, cookies, and deployment notes (Render/Neon).
- README Week 4 highlights added; error catalog includes `NOT_FOUND`; Windows `mvn` usage note for quoted properties on cmd.exe.
- Logging: `AuthController.rateLimited(key)` now logs the rate limit key centrally; removed redundant branch logging.

### Fixed
- Session pruning ClassCastException by replacing derived delete with explicit JPQL bulk delete method and using `@Transactional` in pruner.
- Added Flyway migration `V4__add_timezone_to_users.sql` to align schema with `UserEntity.timezone`, resolving startup error `column ... timezone does not exist`.
- Addressed IDE warnings for the GitHub Release step by pinning the action and passing the `files` input; pipeline runs green on tags.

---

## [v0.1.0] - 2025-10-05 – Initial release
### Added


### Changed


### Fixed


---
Reference: See `docs/weekly-plan/week-3/week-3-checkout-backend.md` and `docs/weekly-plan/week-4/week-4-checkout-backend.md` for narrative weekly summaries.


[Unreleased]: https://github.com/sameboat-platform/backend/compare/v0.1.1...HEAD
[v0.1.1]: https://github.com/sameboat-platform/backend/releases/tag/v0.1.1
[v0.1.0]: https://github.com/sameboat-platform/backend/releases/tag/v0.1.0
