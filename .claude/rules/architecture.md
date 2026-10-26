---
paths:
  - "src/docs/architecture.md"
---
# Phase 2: Architecture and Contracts

## Ownership
Prerequisite: `src/docs/requirements.md` has explicit user approval and is current. Own and edit only `src/docs/architecture.md`. Do not edit source code, tests, other phase documents, workflow configuration, or Jira issues.

## Project Constraints
- Use Java and Spring Boot for REST APIs, with controller/service/repository boundaries.
- Use an interface-backed, thread-safe in-memory repository, normally `ConcurrentHashMap`. Do not design an external database; document restart data loss.
- Use a zero-build static frontend: semantic HTML, vanilla JavaScript, and CSS under `src/main/resources/static/`. Do not introduce React, Vue, Angular, or another heavy framework.
- Do not add unrequested features, authentication, external services, or dependencies. Requirements define product scope.

## Workflow
1. Confirm the requirements artifact is complete, explicitly approved, and current. Stop if approval is missing or upstream documents have changed.
2. Map each requirement ID to the responsible component and architecture decisions. Do not invent unapproved product behavior.
3. Define Spring Boot component responsibilities and request/data flow across controllers, services, repository interfaces, and the in-memory implementation.
4. Specify identity generation, thread safety, atomic mutations, lifecycle, concurrency assumptions, and restart behavior.
5. Define entities, DTOs, validation, date/time semantics, REST methods and paths, request/response examples, status codes, and error contracts.
6. Design the static UI and its API access, states, loading/empty/error behavior, responsiveness, and accessibility. Prefer same-origin calls and justify any CORS need.
7. Document package structure, testing boundaries, security considerations, tradeoffs, and concise Mermaid diagrams. Recommend Java, Spring Boot, and build-tool versions with rationale; mark unapproved choices as proposals.
8. Write `src/docs/architecture.md` with `Status: Draft`, requirement traceability, decisions, risks, open questions, and `## Approvals`. Request explicit approval of components, API, and data model, then STOP before Design Review.

Do not implement code or revise the requirements document. Record upstream changes as invalidating dependent approvals until reapproved.
