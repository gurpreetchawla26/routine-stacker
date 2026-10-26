---
paths:
  - "src/docs/implementation.md"
  - "src/main/java/**"
  - "src/test/**"
  - "src/main/resources/static/**"
  - "pom.xml"
---
# Phase 5: Implementation and Code Execution

## Ownership
Prerequisites: explicitly approved and current requirements, architecture, passing design review, and implementation plan. Own `src/docs/implementation.md` plus only source, test, or configuration files explicitly authorized by approved plan tasks. Do not edit other phase documents, workflow configuration, or Jira issues. Preserve unrelated user changes.

## Project Constraints
- Backend: Java and Spring Boot REST API.
- Storage: thread-safe in-memory repository, normally using `ConcurrentHashMap`; no external database or persistence service. Data resets on restart.
- Frontend: zero-build semantic HTML, vanilla JavaScript, and CSS under `src/main/resources/static/`; no React, Vue, Angular, or other heavy framework.
- Do not introduce unapproved features, dependencies, authentication, external services, or broad refactors. Never bypass project hooks or guards.

## Workflow
1. Confirm all prerequisite artifacts and approvals are current and consistent. Stop if a gate is missing.
2. Inspect relevant code, tests, and user changes. Confirm each task and target file is in the approved plan; request approval before out-of-plan work.
3. Implement tasks in approved order using the selected Java/Spring Boot versions and agreed controller/service/repository boundaries, validations, and API contracts.
4. Keep data in the approved in-memory repository. Implement only the approved static frontend with safe DOM APIs, accessible controls, responsive layout, and required UI states.
5. Add or change only authorized tests. After each meaningful edit, run the cheapest focused check, then planned broader checks when feasible. Record exact commands and actual results.
6. If work requires scope expansion, a new dependency, or changed contract, stop and request a decision. Do not revise upstream documents or disable a guard.
7. Update `src/docs/implementation.md` with `Status: Draft`, approved inputs, completed task IDs, changed files, requirement/test traceability, executed checks and results, deviations, limitations, and `## Approvals`.
8. Request explicit human acceptance of implementation and STOP before Verification.

Clearly distinguish checks run from checks not run. Never claim approval yourself. Do not create branches, commit, push, or publish without separate explicit authorization.
