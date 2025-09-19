# MVP Scope (Fall 2025)

This document defines the **Minimum Viable Product (MVP)** for the SameBoat project.  
It includes user stories, acceptance criteria, and scope boundaries for the first semester build.

---

## 🎯 Goals
- Provide a secure platform where users can **register, log in, and manage profiles**.
- Allow users to **post and view stories** with basic safety (trigger warning) features.
- Display a **basic trust score** to help gauge user credibility.
- Ensure all features are **deployed and accessible** via Netlify (frontend) and Render (backend).

---

## 📖 User Stories + Acceptance Criteria

### 1. Authentication
**Story**  
As a user, I can register and log in so that I can access the platform securely.

**Acceptance Criteria**
- Registration requires a unique email + password.
- Login returns a JWT token.
- JWT is required to access protected endpoints.

---

### 2. User Profile
**Story**  
As a user, I can view and update my profile so others can see who I am.

**Acceptance Criteria**
- Profile includes: name, role (mentor/peer/professional), and bio.
- Only the logged-in user can edit their own profile.
- Profiles are retrievable via `/users/{id}`.

---

### 3. Stories
**Story**  
As a user, I can post and read stories so I can share experiences and connect with others.

**Acceptance Criteria**
- Create story (title, content, trigger flags).
- Read all stories (feed).
- Stories show author + timestamp.
- *Stretch:* Edit/delete own story.

---

### 4. Trust Metric
**Story**  
As a user, I can see a basic trust score on profiles so I can gauge credibility.

**Acceptance Criteria**
- Trust score is calculated with a simple formula (e.g., number of stories + interactions).
- Displayed on profile and story views.
- *Stretch:* Weighted trust metric v2.

---

### 5. Safety / Trigger Warnings
**Story**  
As a user, I can toggle spoiler shields on sensitive stories so I can choose whether to view content.

**Acceptance Criteria**
- Stories can be flagged with trigger categories.
- Frontend hides flagged content until the user clicks "Reveal."
- *Stretch:* User-customizable sensitivity levels.

---

## 🧩 Out of Scope (for MVP)
- Advanced trust metric algorithms.
- Profile pictures and rich customization.
- Moderation/admin tools.
- Semantic search with pgvector.
- UI extras (dark mode, animations).

---

## 📌 Notes
- API contract is owned by this project (we are the **API producer**).
- Contract defined in `openapi/openapi.yaml`.
- Both frontend and backend are auto-deployed from GitHub to Netlify/Render.
- Database: Neon Postgres.
- Deliverable by **Week 8**: All MVP features functional end-to-end.

---