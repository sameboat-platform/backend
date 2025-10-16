# SameBoat Honors Project — Week 5 Plan (Stories MVP)

**Date:** October 09, 2025

---

## 🧭 Current Status

| Area | Current Version | Status | Notes |
|------|-----------------|---------|-------|
| **Backend** | ~v0.2.x | ✅ Auth complete; user CRUD & tests; Flyway, CI, validation in place | Stories endpoint not yet implemented |
| **Frontend** | v0.2.0 | ✅ Authenticated SPA with health monitoring, release automation, accessibility base | Stories feed not yet built |
| **Deployment** | Netlify + Render | ✅ Health/info endpoints live | Need to tag v0.3.x when Stories MVP merges |
| **Testing** | 70 %+ backend / 50 %+ frontend | ✅ CI enforcing gates | Broaden coverage after Stories |
| **Docs / CI** | ARCHITECTURE.md, DATA_MODEL.md, release scripts | ✅ Clean and versioned | Prep 0.3.x docs after MVP |

**Summary:**  
Phase 2 is nearly complete. All infrastructure is ready for the Stories MVP — only the story feature slice itself remains before v1.0.0 MVP.

---

## 🗓 Week 5 Focus — Stories MVP

### 🎯 Goal
Deliver the first user-visible feature beyond authentication: the **Stories** feed (create → view → delete).

### Backend Tasks
- [ ] Create `Story` entity + repository + service.
- [ ] Endpoints:
  - `POST /api/stories` (create)
  - `GET /api/stories` (list recent, paginated)
  - `DELETE /api/stories/{id}` (author-only)
- [ ] Add Flyway migration `V5__create_stories.sql`.
- [ ] Integration tests for create/delete + auth coupling.

### Frontend Tasks
- [ ] `StoriesPage.tsx` using Chakra UI Cards.
- [ ] `StoryForm` + `StoryList` components.
- [ ] API hooks (`useStories`) calling `/api/stories`.
- [ ] Keyboard navigation / ARIA roles verification.
- [ ] Tests for creation/deletion flows.
- [ ] Tag → release v0.3.0 (Stories MVP).

### Documentation & CI
- [ ] Update `DATA_MODEL.md` with Story entity.
- [ ] Add changelog entry under `[Unreleased]` → move to v0.3.0.
- [ ] Verify backend and frontend CI pipelines remain green.
- [ ] Deploy new versions (Render + Netlify).

---

## 📈 Expected Outcome

✅ Authenticated users can:
- Log in / log out.
- Create short “Stories” (1–1000 chars).
- View recent posts (sorted by creation date).
- Delete their own stories.

Deliverables:
- Working Stories MVP deployed on Netlify.
- Tagged versions: **backend v0.3.0** / **frontend v0.3.0**.

---

## 🧩 After MVP

Planned post-MVP stretch goals (Phase 3):
- Trigger-warning system (manual or keyword-based).
- Trust metric prototype.
- Basic user connection / discovery.
- Performance optimization (route-based code splitting).
- Full accessibility pass and presentation polish.

---

**ETA for MVP completion:** ~October 14–18, 2025  
**Next milestone:** Phase 3 (Expanded Features, November 1–20)

---
