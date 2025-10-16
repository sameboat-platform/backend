# SameBoat Backend – Copilot Context & Guard Rails

Purpose: Collaborators (and their AI assistants) should read this file first to understand constraints, architecture, workflows, and prompting patterns. Open this file in an editor tab before asking Copilot for help so it can be included in context.

ALIAS TOKENS (use these in prompts):
- BACKEND_CI_GUARD – Respect main CI workflow guard (see CI section)
- LAYER_RULE – Enforce controller → service → repository layering (no skipping)
- SECURITY_BASELINE – Follow security, validation, and least-privilege rules

---
## 0. First Day Onboarding (Ultra Fast Start)
1. Clone repo & run: `./mvnw -B -ntp clean verify` (Windows: `mvnw.cmd -ntp clean verify`).
2. Open and skim: `pom.xml`, this file, an existing controller (`UserController`), a service (`UserService`), and a migration (if present under `src/main/resources/db/migration`).
3. Start app (dev DB expected local Postgres or H2 depending on profile) – confirm health endpoint `/health` (or check existing `HealthController`).
4. Run an integration test (e.g. `AuthFlowIntegrationTest`).
5. Create a branch: `feat/<short-description>`.
6. For any change: apply prompt preamble: `Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.`
7. Before asking for CI changes: verify whether `.github/workflows/backend-ci.yml` exists locally (list the directory) — do NOT generate a duplicate main CI workflow if it exists.
8. Add at least one test for each new service method or controller endpoint.
9. Keep migrations atomic; never rewrite an old one.
10. Open this file in a tab while using Copilot so it can pick up context.

---
## 1. Core Principles
1. CI workflows: `backend-ci.yml` is the authoritative main pipeline. Additional scoped workflows (e.g., coverage reports, guards) are allowed. If your editor cannot see `backend-ci.yml` (indexing glitch), do not create another main CI file—ask for confirmation first (see Section 18).
2. Layered architecture: Controller -> Service -> Repository (JPA). No direct repository access from controllers. Keep business rules in services.
3. Fail fast, validate early (Bean Validation + explicit defensive checks for security-sensitive paths).
4. Prefer clarity over clever cleverness; minimize hidden side effects.
5. All new logic must have at least one unit or integration test.
6. Immutable, auditable schema migrations via Flyway – never edit an applied migration, always create a new one.
7. Security-first: sanitize, validate, restrict data exposure, and log auth events responsibly.

---
## 2. High-Level Architecture (Current Snapshot)
Spring Boot (v3.5.x, Java 21) project using:
- Spring Web (REST controllers)
- Spring Data JPA (Persistence)
- Spring Security (AuthN/AuthZ – extend carefully)
- Flyway (DB migrations)
- PostgreSQL (runtime) & H2 (tests)
- Testcontainers (dynamic Postgres for selected tests/migrations)
- Lombok (data classes / builders – use judiciously)
- Jacoco (coverage gate: 70% INSTRUCTION)

Assumed Package Layout (expected conventions – maintain consistency):
```
com.sameboat.backend
  ├─ auth/            (authentication, tokens, security config)
  ├─ config/          (Spring @Configuration, properties binding)
  ├─ controller/      (REST endpoints; request/response DTO mapping)
  ├─ domain/          (entities, domain enums)
  ├─ repository/      (Spring Data repositories)
  ├─ service/         (business logic, orchestration)
  ├─ dto/             (API request/response models if not colocated)
  └─ util/            (pure helpers; avoid overuse)
```
If structure deviates, follow established patterns in existing code (open relevant files before prompting Copilot).

### 2.1 Existing Packages (Enumerated Snapshot)
(Keep updated when adding new top-level feature areas.)
```
com.sameboat.backend
  auth/              (Auth controller & principal handling)
  auth/dto/          (Auth request/response DTOs)
  auth/session/      (Session entity, repo, service)
  common/            (Error handling / shared responses)
  config/            (CORS, time, password encoder, properties, datasource logging)
  health/            (Health controller)
  security/          (Security config & filter)
  user/              (User entity, DTO, mapper, repository, service, controller)
  migration (tests)  (Integration test package for Flyway schema checks)
```
If adding a new vertical (e.g. matching, messaging) mirror structure: `entity`, `dto`, `repository`, `service`, `controller`.

---
## 3. Database & Migrations
- Tooling: Flyway via Maven plugin (configured in `pom.xml`).
- Never modify an already-applied migration. Create a new `V<next>__<description>.sql`.
- Use explicit column types; prefer snake_case for DB naming.
- Add constraints (NOT NULL, FK, UNIQUE) early.
- For destructive changes: introduce transitional migrations (add new column, backfill, swap) – no forced downtime.
- Test migrations with Testcontainers profile if adding complex transformations.

---
## 4. Testing Strategy
Types:
- Unit tests (fast, isolate service logic; mock repositories when appropriate)
- Integration tests (SpringBootTest, security filters, serialization)
- Migration test (if profile toggled) using Testcontainers Postgres
Coverage:
- Jacoco minimum: 70% instruction coverage (do not game metrics – write meaningful assertions)
Guidelines:
- Use descriptive test method names: methodName_condition_expectedBehavior
- Avoid logic inside tests (no loops unless data-driven)
- For security endpoints: test success + failure (401/403) paths

### 4.1 Test Templates
Use these as scaffolds (adapt names & packages):

Controller Integration Test (MockMvc):
```java
package com.sameboat.backend.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /users/{id} returns 404 when not found")
    void getUserNotFound() throws Exception {
        mvc.perform(get("/users/{id}", 9999L))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("PATCH /users/{id} updates display name")
    void patchUser() throws Exception {
        // Arrange: (optionally create fixture if test DB seeded)
        // Act + Assert
        mvc.perform(patch("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"New Name\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.displayName").value("New Name"));
    }
}
```

Service Unit Test (mock repository):
```java
package com.sameboat.backend.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTestTemplate {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("getUser throws when user missing")
    void getUserMissing() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(123L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateUserDisplayName persists change")
    void updateDisplayName() {
        UserEntity entity = new UserEntity();
        entity.setId(5L);
        entity.setDisplayName("Old");
        when(userRepository.findById(5L)).thenReturn(Optional.of(entity));

        userService.updateDisplayName(5L, "New");

        assertThat(entity.getDisplayName()).isEqualTo("New");
        verify(userRepository).save(entity);
    }
}
```

Security Filter Test (example sketch):
```java
// Outline only – fill in specifics when adding new filters.
// Use @SpringBootTest + MockMvc or WebMvcTest with sliced configuration.
```

Migration Verification Test (if adding new complex migration):
```java
// Use existing MigrationIntegrationTest pattern; clone & adjust for new assertions.
```

Guidelines:
- Name pattern: <ClassUnderTest>Test or <UseCase>IntegrationTest.
- Include at least one negative path test for each public service method.

---
## 5. Security Baseline (SECURITY_BASELINE)
- Validate all request DTOs with Jakarta Bean Validation annotations.
- Sanitize or whitelist enumerated inputs; never trust client-provided role claims.
- Log authentication events minimally (avoid sensitive payloads or secrets).
- Do not log full exception stacks for expected auth/user errors at INFO – use DEBUG if needed.
- Avoid exposing internal IDs if public-stable identifiers differ.
- Enforce least privilege in service methods (no broad repository fetches when narrower queries suffice).

---
## 6. Coding & Style Guidelines
- Prefer constructor injection (@RequiredArgsConstructor or explicit) over field injection.
- Keep controllers thin: map request -> call service -> map response.
- Services coordinate repositories; no cross-service tight coupling without an interface abstraction.
- Use Optional sparingly at boundaries; internal logic can rely on null checks or explicit exceptions.
- Throw domain-specific exceptions (create simple runtime subclasses) for business rule violations; let a @ControllerAdvice translate them to HTTP status.
- DTO vs Entity: Never expose JPA entities directly from controllers.
- Use records (Java 21) for immutable DTOs where helpful.

---
## 7. Logging
- Use SLF4J (via Lombok @Slf4j or manual logger) – no System.out.
- Log at INFO: startup, major lifecycle events, security boundary transitions.
- Log at WARN: recoverable anomalies.
- Log at ERROR: unexpected failures (avoid duplicate logging same exception up stack levels).
- Never log secrets, tokens, passwords, or PII.

---
## 8. CI Workflow Guard (BACKEND_CI_GUARD) – CRITICAL
Human process + hooks:
- `backend-ci.yml` is the main pipeline. Do not generate another main CI workflow if it exists.
- Additional scoped workflows (like `backend-coverage.yml`, `ci-guard.yml`) are allowed to coexist.
- If `backend-ci.yml` appears missing or your editor cannot see it:
  - Copilot (or collaborator) must respond exactly:
    `backend-ci.yml is missing. Please confirm before I generate any new CI workflow.`
  - Wait for explicit confirmation before proposing any new main CI workflow (IDE indexing can be stale).

Recommended Prompt Preamble Snippet:
```
Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.
```
Add task after that.

---
## 9. Prompting Patterns for Collaborators
Good Patterns:
1. Open this file and relevant code files; then prompt Copilot after writing a comment like:
   // Task: Add service method to fetch active users by role. Apply LAYER_RULE & SECURITY_BASELINE.
2. Provide signature stubs before invoking completions.
3. For refactors: list constraints ("must not change public controller contracts").
4. For tests: paste target method snippet above test class first.

Anti-Patterns (avoid):
- "Write repository + service + controller" (too broad -> low quality)
- Leaving Copilot to guess domain names (always provide entity or DTO names)
- Accepting large unreviewed blocks (review line-by-line; prune logging, validation duplicates)

---
## 10. Adding a New Feature (Checklist)
1. Define request/response DTO(s).
2. Add service method w/ validation & business logic.
3. Add repository query (Spring Data derived or @Query) – prefer derived if simple.
4. Add controller endpoint (minimal mapping code).
5. Add unit test (service) + integration test (controller or security path).
6. Add Flyway migration if schema change.
7. Run: `./mvnw -B -ntp clean verify` locally (ensure coverage still >= 70%).
8. Review logs for noise or sensitive data.
9. Update docs (`api.md` or architecture notes) if new externally visible endpoint.

---
## 11. Flyway & Testcontainers Tips
- For a migration test: enable profile `with-migration-test` or set `-Dskip.migration.test=false`.
- Use idempotent style in data backfills (guard against re-run).
- Keep migrations short & focused; large refactors can be split.

---
## 12. Handling IntelliJ Indexing Issues
Because JetBrains Copilot may fail to see files:
- Reindex: File > Invalidate Caches > Restart.
- Ensure `.github` directory is not excluded.
- Open `backend-ci.yml` (if present) and this file before complex CI prompts.
- If Copilot suggests creating a new workflow unexpectedly, manually verify the folder contents before accepting.

---
## 13. Example Prompt Library (Copy/Paste)
Feature Implementation:
```
Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE.
Add a service method in UserService to deactivate a user by id with audit logging; update controller; generate tests (service + 404 case). Do not expose internal entity directly.
```
Migration:
```
Apply BACKEND_CI_GUARD.
Generate a Flyway migration to add column last_login TIMESTAMP WITH TIME ZONE (nullable) to users table; no data backfill.
```
Security Adjustments:
```
Apply BACKEND_CI_GUARD + SECURITY_BASELINE.
Add method to AuthService to rotate a refresh token; ensure old token invalidated.
```

---
## 14. Don’t Do List
- No new GitHub workflow files (BACKEND_CI_GUARD).
- No direct DB access in controllers.
- No mutation of entities outside service layer.
- No plain-text secrets or test credentials committed.
- No editing historical Flyway scripts.

---
## 15. Reviewing Copilot Output (Human Checklist)
- Correct layering boundaries? (Controller simple; Service owns logic; Repository only persistence.)
- Proper validation? (DTO annotations + service guard rails where needed.)
- Security safe? (No leaked sensitive info, no broad queries.)
- Test coverage added? (At least one positive + one negative path.)
- Migrations immutable & named clearly? (V##__meaningful_snake_case.sql)

---
## 16. Future Enhancements (Meta – not auto-implement)
- Introduce centralized exception handler (@ControllerAdvice) if not present.
- Add structured logging (JSON) for production profile.
- Add performance test harness for critical queries.
- Add OpenAPI generation / validation (if not already wired).

---
## 17. Quick Reference – Minimal Preamble (Ultra-Short)
Use this if token budget is tight:
```
Apply BACKEND_CI_GUARD + LAYER_RULE + SECURITY_BASELINE; follow project conventions.
```

---
## 18. If `backend-ci.yml` Seems Missing
Copilot must output:
```
backend-ci.yml is missing. Please confirm before I generate any new CI workflow.
```
Then wait for explicit confirmation before proposing any new main CI workflow. Additional scoped workflows are permitted and do not change this guard.

---
## 19. Local Dev Commands
```
# Run full verify (tests + coverage)
./mvnw -B -ntp clean verify

# Run with migration test profile
env MAVEN_OPTS="" ./mvnw -Dskip.migration.test=false test
```
(Adjust for Windows: use `mvnw.cmd`.)

---
## 20. Final Reminder
Always open this file + relevant code while prompting Copilot. If suggestions violate BACKEND_CI_GUARD or layering rules, discard and restate constraints more explicitly.

---
## 21. Standard Exception & Error Code Catalog
Current centralized exception handling lives in `common.GlobalExceptionHandler` producing `ErrorResponse { error, message }`.

Existing stable codes (do NOT rename once public without migration notice):
- VALIDATION_ERROR (400) – Bean validation failures on request bodies or method params.
- BAD_REQUEST (400) – Generic client misuse / IllegalArgumentException from service layer.
- BAD_CREDENTIALS (401) – Authentication attempt failed (wrong email/password).
- UNAUTHENTICATED (401) – Missing / invalid / malformed session token.
- SESSION_EXPIRED (401) – Session exists but is expired (security filter / controller pre-checks).
- INTERNAL_ERROR (500) – Unhandled unexpected exception (includes ephemeral ref id in message).

Reserved / Recommended future codes (add when implementing features):
- NOT_FOUND (404) – Resource missing (introduce `ResourceNotFoundException`).
- FORBIDDEN (403) – Authenticated but lacks permission (introduce `AccessDeniedException` mapper if needed).
- CONFLICT (409) – State conflict (duplicate email, version mismatch, etc.).
- RATE_LIMITED (429) – Too many requests (if rate limiting added).

Pattern for adding a new domain exception:
1. Create lightweight runtime exception class (no checked exceptions) – e.g. `public class ResourceNotFoundException extends RuntimeException { public ResourceNotFoundException(String msg){ super(msg);} }`
2. Add `@ExceptionHandler(ResourceNotFoundException.class)` in `GlobalExceptionHandler` mapping to 404 + NOT_FOUND.
3. Service layer throws it; controller remains thin.
4. Add integration/unit tests verifying both success & failure envelope: `{ "error": "NOT_FOUND", "message": "<detail>" }`.
5. Update this catalog (append; never repurpose existing code semantics silently).

Guidelines:
- Keep messages human-friendly but avoid leaking internal entity IDs unless they are intended to be public API identifiers.
- Do not overload the same code for multiple HTTP statuses.
- Log stack traces only for unexpected (500) errors; expected domain exceptions should be INFO or DEBUG at most.

Minimal test assertion template:
```java
mvc.perform(get("/users/{id}", 999L))
   .andExpect(status().isNotFound())
   .andExpect(jsonPath("$.error").value("NOT_FOUND"));
```

When prompting Copilot for new exceptions include: "Add ResourceNotFoundException (NOT_FOUND, 404) and update GlobalExceptionHandler accordingly; extend error code catalog." 

---
## 22. Deployment & Hosting Snapshot (Reference for Prompts)
Use this section when generating code that touches configuration, CORS, cookies, or environment-dependent behavior.

| Component | Provider / Location | Domain / Identifier | Key Settings |
|-----------|---------------------|---------------------|--------------|
| Backend API | Render Web Service | https://api.sameboatplatform.org | Spring profile `prod`, health `/health` |
| Frontend SPA | Netlify | https://app.sameboatplatform.org | Consumes API via `api.` subdomain |
| Root Domains | DNS (Registrar) | sameboatplatform.org / .com | `.com` → `.org` 301 redirect |
| Database | Neon Postgres (Managed) | (Neon project) | JDBC `sslmode=require` (TLS) |
| Auth Session Cookie | Browser (`SBSESSION`) | Domain `.sameboatplatform.org` | `Secure`, `HttpOnly`, `SameSite=Lax` (review for Strict) |
| CORS Allowlist | Spring Config | https://app.sameboatplatform.org | Credentials enabled (cookies) |

### Production Environment Variables (Typical)
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
SAMEBOAT_COOKIE_DOMAIN=.sameboatplatform.org
SAMEBOAT_COOKIE_SECURE=true
SAMEBOAT_CORS_ALLOWED_ORIGINS=https://app.sameboatplatform.org
```

### Prompt Hints
When generating:
- CORS config changes → ensure only `https://app.sameboatplatform.org` added (no wildcard).
- Cookie settings → set domain & Secure flag only in `prod` profile logic (property-driven).
- Database config → do not hardcode credentials; rely on environment variables.
- Links in emails or API docs → use `https://api.sameboatplatform.org`.

### Future (Do NOT Implement Automatically)
- Staging: `staging-api.sameboatplatform.org` + Neon branch database.
- Rate limiting / WAF at edge for auth endpoints.
- Observability: structured logs & metrics export endpoint.

If a prompt involves deployment YAML or CI modifications, apply BACKEND_CI_GUARD first to avoid duplicate workflows.

---
