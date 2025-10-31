Goal (Week 5 – Stories MVP):
Implement a minimal Stories feature end-to-end that fits existing patterns (controllers thin, service owns rules, repo JPA), integrates with the current cookie-session auth, Flyway, error envelope, and test strategy. See existing error codes and envelopes (UNAUTHENTICATED, VALIDATION_ERROR, FORBIDDEN, NOT_FOUND, etc.) and keep consistency. (Refs available in docs/api.md & Architecture.)

Context to respect:
- Auth: opaque session cookie “SBSESSION”; request pipeline already resolves the authenticated user. Use the same way existing /me and guarded endpoints do.
- Flyway: add a new forward-only migration for schema changes (do NOT edit applied files). Next version should be V5__create_stories.sql.
- Error handling: keep centralized mapping for 400/401/403/404 with existing codes and message patterns.
- Profiles/CORS are already wired; new routes live under /api and require authentication.
- Keep controller → service → repository layering.

Data model (suggested; adjust to match codebase conventions):
- StoryEntity:
    - id UUID (PK)
    - authorId UUID (FK → users.id)
    - content TEXT NOT NULL (1..1000)
    - createdAt TIMESTAMP NOT NULL default now()
    - updatedAt TIMESTAMP NULL (set on update)

Repository:
- StoryRepository with feeds that page by createdAt desc (keyset or limit/offset; choose what matches current patterns).
- Query helpers for “before <timestamp>” and “by authorId”.

Service (business rules):
- create(authorId, content) → validate length 1..1000; persist.
- recentFeed(limit=20, before=optional Instant) → ordered desc by createdAt, page correctly.
- myStories(authorId) → ordered desc.
- delete(authorId, storyId) → only if story.authorId == authorId else 403.

Controller (under /api):
- POST /stories { content } → 201 with StoryDto
- GET  /stories?limit=20&before=<ISO> → recent feed (paged)
- GET  /me/stories → current user’s stories
- DELETE /stories/{id} → 204 if owner; 403 otherwise

Validation & errors:
- content length 1..1000 → 400 VALIDATION_ERROR
- unauthenticated → 401 UNAUTHENTICATED
- not owner on delete → 403 FORBIDDEN
- not found → 404 NOT_FOUND
  Use existing GlobalExceptionHandler and validation approach.

DTO/Mapping:
- StoryDto { id, authorId, content, createdAt, updatedAt? }
- Optionally include lightweight author projection if an existing pattern exists; otherwise keep authorId only.

Flyway:
- Create V5__create_stories.sql:
    - stories table, PK on id, FK author_id → users(id), indexes for (created_at desc) and (author_id, created_at desc).
    - Use NOT NULL where appropriate; default now() for created_at.

Tests:
- Service tests: create validates length; delete enforces ownership; feed respects limit & before.
- WebMvc (integration):
    - POST /stories requires auth and returns 201 with body.
    - GET /stories returns recent first and respects limit/before.
    - GET /me/stories returns only current user’s stories.
    - DELETE /stories/{id} returns 204 for owner, 403 for non-owner, 404 for unknown id.
- Reuse existing auth test helpers/conventions; use cookie-based auth setup like /me tests.

Security:
- Wire endpoints into existing security chain as authenticated routes (no new permitAll).
- Preserve CORS/credentials behavior; no changes to global config unless required by tests.

Docs:
- Update docs/api.md with the new endpoints and error cases.
- Add brief README note for the migration and new routes.

Implementation guidance:
- Prefer keyset pagination (createdAt + id) if already used; otherwise simple limit + before=timestamp is acceptable for MVP.
- Keep controller lean; push logic to service.
- Follow repository method naming patterns already present (e.g., findTopNByOrderByCreatedAtDesc or @Query as used elsewhere).
- If a common BaseEntity or auditing exists, reuse that instead of manual timestamps.
- If an AuthPrincipal/RequestContext exists, reuse it to obtain current user id.

Acceptance:
- Authenticated user can POST a story, see a recent feed, list their own stories, and delete only their own; all error envelopes conform to existing conventions; tests green in CI.