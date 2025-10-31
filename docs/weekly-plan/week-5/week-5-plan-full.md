# Week 5 Sprint Plan – SameBoat

**Focus:** Stories MVP (Backend CRUD + Frontend integration scaffolding)

**Sprint Window:** Week 5 (Oct 30 – Nov 5)

**Version Target:** v0.4.0

---

## 🎯 Goal

Implement the foundational Stories feature end-to-end: authenticated users can create short text posts ("stories"), see a feed of recent stories, view their own posts, and delete their own posts. Deliver a working slice integrated with the live backend and Netlify frontend.

Auth and session handling are stable from v0.3.0; this sprint extends functionality into user-generated content.

---

## 🧩 Context

- **Frontend:** React 19 + Vite + TypeScript, Zustand for global state, Netlify deployment.
- **Backend:** Spring Boot 3 + Postgres (Neon), Flyway migrations, Render deployment.
- **Auth:** Cookie-based session (`SBSESSION`) validated across Netlify ↔ Render.
- **CI/CD:** Both pipelines stable and enforced; main branch protected.
- **Domains:** app = Netlify; api = Render (CORS already configured).

---

## 🛠️ Scope

### Backend – Story CRUD API

**Entity:** `Story`
- `id UUID`
- `authorId UUID`
- `content TEXT (1–1000)`
- `createdAt TIMESTAMP`
- `updatedAt TIMESTAMP NULLABLE`

**Migration:** `V5__create_stories.sql`

**Endpoints:**
| Method | Path | Description |
|---------|------|-------------|
| `POST` | `/api/stories` | Create story (auth required) |
| `GET` | `/api/stories?limit=20&before=<ISO>` | Fetch recent stories feed |
| `GET` | `/api/me/stories` | Get current user’s stories |
| `DELETE` | `/api/stories/{id}` | Delete story (owner only) |

**Validation & Errors:**
- 400 `VALIDATION_ERROR` → invalid length (1–1000)
- 401 `UNAUTHENTICATED` → no session
- 403 `FORBIDDEN` → delete non-owner
- 404 `NOT_FOUND` → story missing

**Backend Checklist:**
- [ ] Add `Story` entity, repository, and service.
- [ ] Add `StoryController` under `/api/stories`.
- [ ] Implement ownership enforcement on DELETE.
- [ ] Wire routes into SecurityConfig (auth required).
- [ ] Add Flyway migration `V5__create_stories.sql`.
- [ ] Add integration tests (create/auth required/pagination/delete).
- [ ] Add OpenAPI annotations and update `api.md`.

---

### Frontend – Story Feed & Compose

**Routes:** `/feed`, `/compose`, `/me/posts`

**Components:**
- `ComposeForm` → textarea (1–1000 chars) + counter + submit.
- `StoryList` → renders stories, preserves newlines, HTML-escaped.
- `StoryItem` → single story (author, content, date, delete button if owner).

**API Helpers:**
- `createStory(content)` → `POST /stories`
- `getFeed(params)` → `GET /stories`
- `getMyStories()` → `GET /me/stories`
- `deleteStory(id)` → `DELETE /stories/{id}`

**Frontend Checklist:**
- [ ] Create `api/stories.ts` client methods using `fetch` with `credentials:'include'`.
- [ ] Implement `ComposeForm` with validation and optimistic update.
- [ ] Build `StoryList` component (feed rendering + delete hook).
- [ ] Add `/feed`, `/compose`, `/me/posts` routes.
- [ ] Implement delete confirmation + error toast (403 handling).
- [ ] Wire Zustand or context integration for current user + stories.
- [ ] Update `App.tsx` navigation for new routes.
- [ ] Verify Netlify preview shows live feed hitting Render API.

---

### 🧪 Tests

**Backend Tests:**
- [ ] Create story requires auth; content length validated.
- [ ] Feed returns latest-first, pagination correct.
- [ ] Delete only by owner; 403 for others.

**Frontend Tests (Vitest + RTL):**
- [ ] ComposeForm validation → blocks >1000 chars & empty.
- [ ] Feed renders list; optimistic create works.
- [ ] Delete removes story on 204; shows error toast on 403.
- [ ] Protected routes still pass after new additions.

---

### ⚙️ CI/CD & Docs
- [ ] Extend backend CI with Story tests in `mvn verify`.
- [ ] Extend frontend CI with new tests + coverage gate.
- [ ] Verify deploy pipeline auto-triggers for both services.
- [ ] Update `README.md` → add Stories API reference.
- [ ] Update `CHANGELOG.md` → add v0.4.0 (Stories MVP start).
- [ ] Update `/docs/api.md` with Story endpoints.

---

### 🧱 Stretch Goals (If Time Allows)
- [ ] Add simple pagination button ("Load more") in `/feed`.
- [ ] Add `updatedAt` display for edited stories.
- [ ] Empty-state UX for `/feed` and `/me/posts`.
- [ ] Light accessibility audit (keyboard nav + ARIA labels).

---

## 🚧 Blockers / Dependencies
- None currently blocking; backend and auth layers stable.
- Ensure backend deployment database is migrated before frontend tests run.

---

## ✅ Acceptance Criteria
- Authenticated user can post, view, and delete their own stories.
- Backend enforces validation and ownership correctly.
- Feed displays stories in reverse chronological order.
- All new tests pass locally and in CI.
- Netlify preview works against live Render API.

---

**Milestone Outcome:** Stories MVP backend + minimal frontend integration complete → ready for Week 6 (Stories UX polish + Feed pagination + delete confirmations).