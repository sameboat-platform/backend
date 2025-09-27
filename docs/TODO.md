# TODO / Backlog (Post Week 2 Enhancements)

This backlog captures agreed future improvements not yet implemented. Items are grouped by area and tagged with suggested priority (P1 highest). Date: 2025-09-27

---
## 1. Migration / CI Tooling
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| MIG-001 | PR Comment Annotation | Post a PR comment summarizing migration immutability check results (pass/fail + changed files) | P2 | Use GitHub Actions + gh cli or workflow command outputs |
| MIG-002 | Failure Annotation | Add GitHub summary + error annotations listing disallowed modified migrations | P1 | Use ::error annotations looping over offending files |
| MIG-003 | Next Migration Helper | Script `scripts/next-migration.sh` that inspects existing V# and prints next version with optional stub template | P3 | Could accept description slug -> generates V{n+1}__{slug}.sql |
| MIG-004 | Parallel Migration Job | Split migration schema test into a separate workflow job to run concurrently with main build | P2 | Speeds PR feedback; artifact not required |

---
## 2. Developer Experience
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| DEV-001 | run-dev.ps1 | Convenience script mirroring run-dev.cmd plus optional parameter overrides | P2 | Detect missing Docker & suggest start command |
| DEV-002 | run-dev.sh | Bash equivalent for cross-platform parity | P3 | Optional if contributors on Linux/macOS |
| DEV-003 | Enforce Exec Bits | CI check ensures *.sh hooks/scripts are executable (prevent accidental chmod loss) | P3 | Simple find + test -x loop |

---
## 3. Observability / Logging
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| OBS-001 | Central Error Handler | Add `@ControllerAdvice` returning standardized `{error,message}` JSON | P1 | Aligns with API reference section 3 |
| OBS-002 | Structured Logging | Introduce log pattern with trace/correlation id (MDC) | P3 | Provide future distributed tracing readiness |

---
## 4. Authentication Roadmap
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| AUTH-001 | Password Hashing | Replace stub `dev` password with BCrypt + user creation flow | P1 | Pre-req for registration feature |
| AUTH-002 | Registration Endpoint | `POST /auth/register` minimal payload (email, password) | P2 | Requires AUTH-001 |
| AUTH-003 | JWT Transition Prototype | Issue short-lived access + refresh tokens, maintain revocation list or reuse sessions table | P2 | Evaluate complexity vs current opaque session |

---
## 5. Testing & Coverage
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| TEST-001 | Coverage Badge | Generate Jacoco badge and publish to README | P3 | Use shields.io or GitHub Pages artifact |
| TEST-002 | Mutation Testing (Optional) | Integrate PIT for critical packages (auth, session) | P4 | Only if time permits; track mutation score |

Sanity test:
Open a PR that edits an old migration → CI fails → PR shows required check failing → merge button blocked.
Open a PR that only adds a new migration → CI passes → merge allowed.
Name it something clear like “Protect main (PR + CI)” and you’re done.

---
## 6. Documentation Enhancements
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| DOC-001 | API Versioning Note | Add strategy section for future /api/v1 path introduction | P3 | Clarify migration path for clients |
| DOC-002 | Architecture Diagram | High-level C4 container diagram (backend, DB, future services) | P3 | PlantUML or Mermaid for diffability |

---
## 7. DX Quality Gates
| ID | Item | Description | Priority | Notes |
|----|------|-------------|----------|-------|
| GATE-001 | Pre-push Hook | Optional pre-push that runs fast immutability + lint | P3 | Keep optional to avoid friction |
| GATE-002 | Secret Scan | Add GitHub Action (gitleaks) to detect accidental secrets | P2 | Protect Neon credentials |

---
## 8. Risk Tracking Candidates
| Risk | Mitigation Candidate ID |
|------|-------------------------|
| Editing applied migrations | MIG-001/MIG-002 notifications reinforce policy |
| Auth stub lingering into later weeks | AUTH-001 priority ensures replacement |
| Inconsistent error payloads | OBS-001 central handler |

---
## 9. Prioritized Near-Term (Suggested Order)
1. OBS-001 (central error handler)
2. AUTH-001 (password hashing)
3. MIG-002 (failure annotations) + MIG-001 (PR comment)
4. AUTH-002 (registration) / MIG-003 (next migration helper)
5. DEV-001 (run-dev.ps1)

---
## 10. Implementation Notes (Guidelines)
- Keep scripts POSIX-compliant (bash) + pwsh for Windows parity.
- Prefer adding new workflow steps instead of expanding existing ones to keep logs segmented.
- Tag PRs that implement backlog items with the ID (e.g., [MIG-002]).
- For JWT prototype (AUTH-003), evaluate storing refresh tokens in existing `sessions` table vs new `refresh_tokens` for audit clarity.

---
## 11. Completed Items Log (Append Here When Done)
| Date | ID | PR | Notes |
|------|----|----|-------|

---
(End of Backlog)

