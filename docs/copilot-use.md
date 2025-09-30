# GitHub Copilot Instructions for SameBoat Backend

Welcome to the SameBoat backend project! This document provides guidelines and best practices for using GitHub Copilot effectively within this repository.

## 1. General Usage
- Use Copilot to assist with code completion, boilerplate generation, and suggestions for Java, YAML, and shell scripts.
- Always review Copilot's suggestions for accuracy, security, and style before committing.
- Prefer Copilot for repetitive code, test scaffolding, and documentation drafts.

## 2. Project Structure
- The backend is a Maven-based Java project. Main code is under `src/main/java`, tests under `src/test/java`.
- Configuration files are in `src/main/resources` and `src/test/resources`.
- CI/CD is managed via `.github/workflows/backend-ci.yml`.

## 3. Copilot for CI/CD
- Use Copilot to draft or update GitHub Actions workflows, but always align with the existing `backend-ci.yml`.
- Ensure any workflow changes maintain the steps for permissions, JDK setup, script permissions, migration checks, and Maven build/test.

## 4. Copilot for Java Code
- Use Copilot to:
    - Suggest method implementations, especially for service, controller, and repository layers.
    - Generate interface and class stubs.
    - Write unit and integration tests.
- Review for:
    - Correct use of Spring Boot, JPA, and other frameworks.
    - Proper exception handling and logging.
    - Adherence to project conventions and Java best practices.

## 5. Copilot for Documentation
- Use Copilot to help draft Markdown files in `docs/`, but ensure all content is accurate and project-specific.
- Summarize code, explain architecture, or generate API documentation drafts.

## 6. Security and Privacy
- Never accept Copilot suggestions that expose secrets, credentials, or sensitive data.
- Review for potential security vulnerabilities, especially in authentication, authorization, and data handling code.

## 7. Limitations
- Copilot may not always understand project-specific logic or custom interfaces. Always verify suggestions.
- Do not use Copilot to generate legal, compliance, or policy documents without human review.

## 8. Troubleshooting
- If Copilot suggestions are irrelevant, try providing more context or writing a descriptive comment.
- For IntelliJ indexing issues, try reindexing the project or restarting the IDE.

## 9. Contribution
- All Copilot-assisted code must be reviewed by a project maintainer before merging.
- Document any significant Copilot-generated logic in code comments or PR descriptions.

---

For more information, see the [README.md](../README.md) and [docs/](../docs/) directory.