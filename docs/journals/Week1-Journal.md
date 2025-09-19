Week 1  
9/13/2025

Tasks Completed:
- Created two repos: [sameboat-frontend](https://github.com/ArchILLtect/sameboat-frontend) and [sameboat-backend](https://github.com/ArchILLtect/sameboat-backend).
- Scaffolded both repos (frontend with Vite + React + TS, backend with Spring Boot 3 + Java 21).
- Installed core backend dependencies (Spring Web, JPA, Validation, Lombok, Flyway, PostgreSQL Driver, DevTools).
- Set up Neon PostgreSQL database, created initial `sameboat` DB, and ran Flyway migration V1__init.sql with `users`, `stories`, and `trust_events` tables.
- Fixed DB role/privilege issues and confirmed migrations ran successfully.
- Configured CI/CD workflows in GitHub Actions:
    - Backend → `mvn verify` with JDK 21.
    - Frontend → `npm ci && npm run build` with Node 20.
- Added repo issue templates and labels for both repos.
- Added `/docs` folder (Architecture.md, ProjectPlan.md, reflections).
- Exported semester plan calendar from Motion → synced with Google Calendar → added `.ics` file and screenshots to repo under `/schedule`.

Reflection:  
This week was all about laying down the foundation. It felt amazing to hit that “green checkmark” in CI for both repos — I actually celebrated out loud when the badges turned green. The database setup gave me a few headaches (psql quirks in PowerShell, Flyway permission errors), but working through that made me more confident about handling DB issues later. I also made my first big Git mistake (committing before pulling) and had to fix divergence with `git fetch + rebase`. It was frustrating at that moment, but a great learning experience.

Biggest win? Seeing the repos fully structured, DB initialized, CI/CD passing, and docs + calendar all organized. It feels like a *real project* now, not just an idea. Honestly, I’m still riding that high.

Next week’s focus: auth stubs + basic user profiles. Time to move from foundation into actual feature work. WOOHOO! Finally!  