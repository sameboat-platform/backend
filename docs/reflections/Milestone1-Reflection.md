# Milestone 1 Reflection – Week 1 (Fall 2025)

## Goals for the Week
The first milestone of the SameBoat project was about establishing strong foundations. My objectives were:

- Create separate repos for frontend (**Vite + React + TS**) and backend (**Spring Boot + Java 21**).
- Set up scaffolds with core dependencies (Spring Web, JPA, Validation, Lombok, Flyway, PostgreSQL Driver, DevTools).
- Initialize the database schema with Flyway migration `V1__init.sql`.
- Configure Neon PostgreSQL as the backend DB and test least-privilege access.
- Add **CI pipelines** for both repos (GitHub Actions), ensuring builds run on every push/PR.
- Add **documentation structure** (`docs/` folder, architecture overview, project plan).
- Add **repo labels and issue templates** for smoother collaboration.
- Begin tracking schedule and tasks in Motion + export/share calendar for advisor review.

---

## What Went Well
✅ Both repos are fully scaffolded and pushed to GitHub with clean, minimal setups.  
✅ Backend successfully ran Flyway V1 migration and confirmed schema (users, stories, trust_events).  
✅ CI pipelines are live:
- Backend runs `mvn verify` with JDK 21.
- Frontend runs `npm ci && npm run build` with Node 20.

✅ Early CI issues (Maven test failures, badge caching) were resolved, and both CI badges now show **passing**.  
✅ Documentation structure is in place:
- `docs/Architecture.md` → MVP overview.
- `docs/ProjectPlan.md` → 12-week semester action plan.
- `docs/schedule/` → Semester calendar `.ics`, screenshots, and links to Motion/Google Calendar.

✅ Repo labels and issue templates added for both frontend and backend.  
✅ Calendar integration: semester plan is synced/exported, shared with advisor, and committed to repo (with screenshots for backup).  
✅ Added a **Kanban view (screenshots + Motion link)** for visual tracking of milestones.

---

## Challenges Faced
- **Database setup quirks:** Running `psql` in PowerShell unexpectedly opened a new terminal, but I confirmed it still executed properly.
- **Flyway init warnings:** Saw `"pgcrypto already exists"` messages — verified migrations still applied successfully.
- **Git mistakes:** Forgot to pull before committing → hit divergence issues. Fixed using `git fetch + git rebase origin/main`.
- **CI test failures:** Spring Boot’s autoconfiguration in `application-test.properties` broke smoke builds. Solved by adding a dedicated test profile that disables JPA/DB during CI.
- **Badge confusion:** GitHub Actions badge showed failing even after green runs. Fixed with `?branch=main` query param.
- **Calendar syncing:** Motion tasks weren’t appearing in Google Calendar until I found the setting to “Show tasks on Google & Outlook Calendar.” Once flipped, sync worked correctly.

---

## Lessons Learned
- Start with the **least privilege DB users** — makes security a first-class concern.
- Flyway requires careful versioning to avoid migration drift; naming discipline matters.
- **Git rebase** > merge for keeping `main` history clean.
- GitHub badges can mislead if not scoped correctly.
- Laying down **CI early** pays off immediately — it caught misconfigurations and prevented regressions.
- Motion → Google Calendar sync isn’t automatic; you must explicitly enable task export. Worth the troubleshooting effort for advisor transparency.

---

## Next Steps (Week 2–3)
- Implement **Auth stubs** (basic login/session, JWT later).
- Add **User profile basics + API endpoints**.
- Extend test coverage beyond smoke tests (eventually add DB-integrated tests).
- Continue documenting decisions in `/docs`.
- Keep updating advisor with synced calendar and milestone progress.

---

✅ **Status: Milestone 1 complete** — Foundations are solid: backend + frontend scaffolds, CI/CD pipelines, docs, schedule/calendar, and advisor-friendly planning are all in place. The project is ready to advance into **Auth + Profiles (Milestone 2)**. 