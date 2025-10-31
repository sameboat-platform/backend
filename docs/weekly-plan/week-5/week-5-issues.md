# SameBoat Backend – Week 5 Issue Drafts (Stories MVP)

This document contains ready-to-copy GitHub issue drafts for Week 5. Each issue is structured using the closest matching GitHub ISSUE_TEMPLATE in `.github/ISSUE_TEMPLATE/`.

---

## 1) Stories DB Migration – V5__create_stories.sql
Template: feature.yml

- Summary: Create Flyway migration `V5__create_stories.sql` to add `stories` table with `id uuid PK`, `author_id uuid NOT NULL FK → users(id)`, `content text NOT NULL (1..1000)`, `created_at timestamptz NOT NULL default now()`, `updated_at timestamptz NULL`; add indexes on `(created_at desc)` and `(author_id, created_at desc)`.
- Motivation / Problem: Enable Stories domain persistence following Flyway immutability and project naming/constraints.
- Acceptance Criteria:
  - Migration file added under `src/main/resources/db/migration/` and never modifies historical migrations.
  - Table schema and constraints match plan.
  - App boots and Flyway applies V5 successfully in local and CI.
- Target Release: 0.1.2
- Labels: area:stories, area:db, backend, db, type:feature, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 2) Stories Domain: Entity + DTO + Mapper
Template: feature.yml

- Summary: Add `StoryEntity`, `StoryDto`, `CreateStoryRequest`, and `StoryMapper`.
- Motivation / Problem: Establish domain and API models for the Stories MVP with strict separation of entity vs DTO.
- Acceptance Criteria:
  - `StoryEntity` with fields: `id`, `authorId`, `content`, `createdAt`, `updatedAt`; `@Table(name="stories")`; timestamps via `@PrePersist/@PreUpdate`.
  - DTOs: `StoryDto { id, authorId, content, createdAt, updatedAt }`, `CreateStoryRequest { content }` with Bean Validation `@NotBlank @Size(min=1, max=1000)`.
  - `StoryMapper` entity ↔ dto helpers.
  - No controller returns JPA entities directly (DTO only).
- Target Release: 0.1.2
- Labels: area:stories, backend, type:feature, java, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 3) StoryRepository Queries
Template: feature.yml

- Summary: Create `StoryRepository` with recent-feed and by-author queries ordered by `createdAt` DESC.
- Motivation / Problem: Efficient retrieval for feed and user’s own stories.
- Acceptance Criteria:
  - Methods for: recent feed with optional `before` timestamp and limit; by author ordered DESC.
  - Uses Spring Data JPA derived queries or `@Query` as needed; no native SQL required.
- Target Release: 0.1.2
- Labels: area:stories, backend, type:feature, java, roadmap
- Notes / Links:
docs/weekly-plan/week-5/week-5-plan-backend.md
docs/weekly-plan/week-5/week-5-plan-issues.md

---

## 4) StoryService Business Rules
Template: feature.yml

- Summary: Implement `StoryService` with `create`, `recentFeed`, `myStories`, and `delete` enforcing validation and ownership.
- Motivation / Problem: Centralize Stories logic per LAYER_RULE with SECURITY_BASELINE validation and least privilege.
- Acceptance Criteria:
  - `create(authorId, content)` validates length 1..1000 and persists.
  - `recentFeed(limit, before)` clamps `limit` to [1..50], default 20; filters by `before` if present; returns newest first.
  - `myStories(authorId)` returns newest first for that author.
  - `delete(authorId, storyId)` deletes only if owner; otherwise throws `AccessDeniedException`; throws `ResourceNotFoundException` if missing.
  - No controller accesses repositories directly.
- Target Release: 0.1.2
- Labels: area:stories, backend, type:feature, java, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 5) StoryController Endpoints (Authenticated)
Template: feature.yml

- Summary: Add REST endpoints: `POST /api/stories`, `GET /api/stories?limit&before`, `GET /api/me/stories`, `DELETE /api/stories/{id}`.
- Motivation / Problem: Expose Stories MVP via thin controller mapping to service using `AuthPrincipal`.
- Acceptance Criteria:
  - POST validates `CreateStoryRequest` and returns `201 Created` with `StoryDto`.
  - GET `/api/stories` supports `limit` and ISO-8601 `before`; returns newest-first.
  - GET `/api/me/stories` returns only current user’s stories.
  - DELETE enforces ownership: 204 on owner; 403 otherwise; 404 if missing.
  - All endpoints require authentication; no new permitAll rules.
- Target Release: 0.1.2
- Labels: area:stories, api, backend, type:feature, java, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 6) Exception Mapping: 403 FORBIDDEN
Template: feature.yml

- Summary: Map `AccessDeniedException` to `403 FORBIDDEN` with `{ error: "FORBIDDEN", message: "..." }` in `GlobalExceptionHandler`.
- Motivation / Problem: Ensure consistent error envelopes for authorization failures on delete.
- Acceptance Criteria:
  - Add `@ExceptionHandler(AccessDeniedException.class)` mapping to 403 code `FORBIDDEN`.
  - No change to existing codes or mappings.
  - Negative-path tests assert correct envelope.
- Target Release: 0.1.2
- Labels: area:stories, backend, type:feature, java, security, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 7) Stories – Service Unit Tests
Template: test.yml

- Area / Component: `StoryService` (create, recentFeed, myStories, delete)
- Test Cases:
  - create rejects length 0 and >1000.
  - delete throws `AccessDeniedException` for non-owner; deletes for owner.
  - recentFeed clamps limit and respects `before`; returns newest-first ordering.
- Target Release: 0.1.2
- Labels: area:stories, backend, type:test, java, roadmap

---

## 8) Stories – Controller Integration Tests (MockMvc)
Template: test.yml

- Area / Component: Stories endpoints (`POST /api/stories`, `GET /api/stories`, `GET /api/me/stories`, `DELETE /api/stories/{id}`)
- Test Cases:
  - Unauthenticated requests return `401 UNAUTHENTICATED` envelope.
  - POST creates story (201) and returns body with timestamps.
  - GET feed respects `limit` and `before`; newest-first.
  - GET me/stories returns only self stories.
  - DELETE: 204 owner; 403 non-owner; 404 unknown id.
- Target Release: 0.1.2
- Labels: area:stories, api, backend, type:test, java, roadmap

---

## 9) OpenAPI + Docs Update for Stories
Template: feature.yml

- Summary: Update `openapi/sameboat.yaml` and docs (`docs/api.md`, `README.md`) to include Stories endpoints and error envelopes.
- Motivation / Problem: Keep API contract documented and discoverable for frontend integration.
- Acceptance Criteria:
  - OpenAPI includes POST/GET feed/GET me/DELETE with request/response schemas and error envelopes.
  - docs/api.md shows sample requests/responses and error cases.
  - README adds short Stories note and migration reference.
- Target Release: 0.2.0
- Labels: area:stories, api, backend, type:docs, documentation, roadmap
- Notes / Links: docs/weekly-plan/week-5/week-5-plan-backend.md

---

## 10) CI/Verification: Migrations + Coverage ≥ 70%
Template: tooling.yml

- Summary: Ensure `mvn -ntp clean verify` runs migrations and tests for Week 5 additions, maintaining ≥ 70% instruction coverage.
- Details / Plan:
  - Confirm no changes to main CI workflow (BACKEND_CI_GUARD).
  - Run full verify locally and in CI; ensure Flyway applies V5.
  - Address any coverage dips with targeted tests.
- Target Release: 0.1.2
- Labels: area:stories, area:ci, backend, type:tooling, infra, roadmap

---

Notes:
- Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE across all issues.
- Keep migrations immutable; no edits to V1–V4.
