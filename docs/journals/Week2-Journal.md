Week 2  
9/20/2025

Tasks Completed:
- Implemented BCrypt password hashing and removed dev stub fallback.
- Added user registration endpoint (`POST /auth/register`) returning `{userId}` + session cookie.
- Introduced distinct auth error codes: `UNAUTHENTICATED`, `BAD_CREDENTIALS`, `SESSION_EXPIRED`, `EMAIL_EXISTS`.
- Added alias session cookie support (`SBSESSION` primary, `sb_session` accepted).
- Created prod profile (`application-prod.yml`) with Secure cookie, domain `.sameboat.<tld>`, 14‑day TTL, CORS origins list.
- Converted scattered `@Value` injections to type‑safe `SameboatProperties`.
- Implemented session authentication filter: expiry detection + context population.
- Expanded integration test suite (registration, duplicate email, wrong password, expired session (primary + alias), garbage cookie, logout, patch validations).
- Raised and passed JaCoCo coverage gate at 70%.
- Updated API docs & README (error codes, configuration properties, registration flow).
- Added Week 2 plan outcome + deviation changelog, updated domain/hosting checklist.

Reflection:
Week 2 escalated from a simple stub plan into a nearly production‑like auth slice. Pulling registration and hashing forward felt risky mid‑week, but it eliminated technical debt early. The biggest surprise was how much simpler everything became once I centralized configuration (`SameboatProperties`)—it reduced mental load and killed a bunch of IDE warnings. Distilling error responses into clear codes (BAD_CREDENTIALS vs UNAUTHENTICATED vs SESSION_EXPIRED) already pays off in cleaner tests and will help the frontend. I consciously deferred rate limiting, password complexity enforcement, and session pruning; they’re now concrete Week 3 targets. I also under‑invested in pure unit tests (leaned heavily on integration); raising coverage again will need more focused service‑level tests. Overall, the project now feels like it has a solid, extensible authentication core instead of a throwaway stub.

Biggest Win:
Delivering a real registration + hashed password flow a week earlier than planned while still keeping tests green and docs updated.

Challenges / Lessons:
- Transition from stub to real hashing required cleaning up legacy helper (`findOrCreateByEmail`).
- Remembering to align docs & error code names (UNAUTHENTICATED vs original UNAUTHORIZED) showed the value of a final consistency pass.
- Integration tests give confidence, but I need more granular unit tests to safely refactor later (Week 3 action).

Next Week Focus (Week 3 Preview):
- Password complexity validation & rate limiting (introduce `RATE_LIMITED`).
- Session pruning scheduler.
- Frontend minimal auth UI (login + /me + 401 redirect).
- Add migration test to default CI path + bump coverage toward 75%.
- JWT vs extended sessions spike & decision doc.

Risks to Watch:
- Session table growth (pruning not yet implemented).
- Lack of rate limiting (brute force exposure until added).
- Coverage plateau if unit tests aren’t prioritized early in the week.

Mood: Energized—shipping real auth earlier de‑risked a lot; ready to tighten security & polish DX in Week 3.

