---
paths:
  - "src/docs/requirements.md"
---
# Phase 1: Requirements and Scope

## Ownership
Own and edit only `src/docs/requirements.md`. Do not edit application code, tests, other phase documents, workflow configuration, or Jira issues. Do not decide product behavior or grant approvals on the user's behalf.

## Project Constraints
- Backend is Java with Spring Boot REST APIs.
- Data is stored only in a thread-safe in-memory repository, normally backed by `ConcurrentHashMap`; all data is lost on restart.
- Frontend is zero-build semantic HTML, vanilla JavaScript, and CSS served from `src/main/resources/static/`.
- Do not propose an external database or a heavy frontend framework such as React, Vue, or Angular. Do not add unrequested authentication, services, dependencies, or features.

## Workflow
1. Read the user's request, existing requirements, and applicable project instructions. Preserve prior user-approved scope unless the user changes it.
2. Identify users, goals, in-scope behavior, exclusions, assumptions, and ambiguities. Ask focused questions when an unresolved choice materially changes scope; label proposed defaults as assumptions.
3. Write user stories for requested behavior only. Assign stable IDs such as `REQ-001` and create measurable Given/When/Then acceptance criteria, including relevant validation and edge cases.
4. Capture non-functional needs such as accessibility, responsive behavior, errors, concurrency, and expected use where relevant. State plainly that in-memory data resets on restart.
5. Add constraints, dependencies, risks, open questions, out-of-scope items, and a matrix linking requirements to acceptance criteria.
6. Write the complete artifact with `Status: Draft` and an `## Approvals` section. Record approval only after explicit user confirmation, including date, scope, and notes.
7. Summarize decisions and unresolved questions, ask the user to approve the requirements, and STOP. Do not begin Architecture or imply Phase 2 is approved.

If approved requirements later change, note that dependent phase approvals are stale. Never invent approval or advance automatically.
