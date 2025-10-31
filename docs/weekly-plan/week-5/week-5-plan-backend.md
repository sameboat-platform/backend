# Week 5 – Backend Plan (Final Draft): Stories MVP

Applies BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE; follow project conventions.

## Goal
Implement Stories MVP end-to-end on the backend so authenticated users can create short text posts, fetch a recent feed, list their own stories, and delete their own posts. Keep controllers thin, services own rules, and repositories JPA.

## Context & Constraints
- Auth: cookie session `SBSESSION`; reuse existing authentication flow and `AuthPrincipal` pattern.
- Flyway: add new forward-only migration `V5__create_stories.sql` (never edit past scripts).
- Error handling: centralized envelopes `{ "error": CODE, "message": ... }` with existing codes.
- Security: new routes under `/api`, all require authentication; no new permitAll.
- Layering: Controller → Service → Repository (no direct repo access from controllers).

## Data Model
Entity: `StoryEntity`
- `id UUID` (PK)
- `authorId UUID` (FK → users.id)
- `content TEXT` NOT NULL (1..1000)
- `createdAt TIMESTAMP WITH TIME ZONE` NOT NULL default now()
- `updatedAt TIMESTAMP WITH TIME ZONE` NULL (set on update)

DTO: `StoryDto { id, authorId, content, createdAt, updatedAt? }`
Mapper: `StoryMapper` (entity ↔ dto)

## Repository
`StoryRepository` (Spring Data JPA):
- Recent feed: ordered by `createdAt` desc (keyset with `before` timestamp recommended; limit enforced)
- By author: ordered desc

## Service (Business Rules)
- `create(authorId, content)` → validate length 1..1000; persist.
- `recentFeed(limit=20, before=optional Instant)` → ordered desc by createdAt; page correctly.
- `myStories(authorId)` → ordered desc.
- `delete(authorId, storyId)` → only owner may delete; else throw `AccessDeniedException` mapped to 403.
- Throw `ResourceNotFoundException` when story not found (maps to 404 NOT_FOUND).

## Controller (under /api)
- `POST /stories` { content } → 201 + `StoryDto`
- `GET /stories?limit=20&before=<ISO>` → recent feed (paged)
- `GET /me/stories` → current user’s stories
- `DELETE /stories/{id}` → 204 if owner; 403 otherwise (and 404 if missing)

## Validation & Errors
- Content length 1..1000 → 400 `VALIDATION_ERROR`
- Unauthenticated → 401 `UNAUTHENTICATED`
- Not owner on delete → 403 `FORBIDDEN`
- Not found → 404 `NOT_FOUND`
(Use existing `GlobalExceptionHandler` and Bean Validation.)

## Flyway Migration
Create `src/main/resources/db/migration/V5__create_stories.sql`:
- Table `stories` with columns per model above
- PK on `id`
- FK `author_id` → `users(id)`
- Index on `(created_at desc)` and `(author_id, created_at desc)`

## Tests
- Service unit tests:
  - create validates length
  - delete enforces ownership
  - feed respects `limit` and `before`
- WebMvc integration tests:
  - POST /stories requires auth; 201 with body on success
  - GET /stories returns recent-first; respects `limit`/`before`
  - GET /me/stories returns only current user’s stories
  - DELETE /stories/{id} → 204 owner, 403 non-owner, 404 unknown id

## OpenAPI & Docs
- Update `openapi/sameboat.yaml` with new `/api/stories` endpoints and error envelopes.
- Update `docs/api.md` examples and `README.md` with a brief Stories note and migration reference.

## CI/CD
- Ensure `mvn verify` covers new tests (keep coverage ≥ 70%).
- No changes to main CI workflow (BACKEND_CI_GUARD).

## Checklist
- [ ] Entity + DTO + Mapper created
- [ ] Repository methods for feed/by-author
- [ ] Service methods: create, recentFeed, myStories, delete (ownership)
- [ ] Controller routes: POST/GET/GET(me)/DELETE
- [ ] Flyway migration V5__create_stories.sql
- [ ] Unit + integration tests added and green
- [ ] OpenAPI + docs updated (api.md, README)
- [ ] CI green; coverage ≥ 70%

## Acceptance Criteria
- Authenticated user can post, view recent feed, list own stories, and delete only their own.
- Error envelopes conform to existing conventions.
- All new tests pass locally and in CI.

