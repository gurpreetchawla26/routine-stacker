---
paths:
  - "src/docs/implementation-plan.md"
---
# Phase 4: Implementation Planner and Task Breakdown

## Ownership
Prerequisites: explicitly approved, current requirements and architecture, and a passing, approved design review. Own and edit only `src/docs/implementation-plan.md`. Do not edit source code, tests, other phase documents, workflow configuration, or Jira issues.

## Project Constraints
- Plan for Java/Spring Boot REST APIs, a thread-safe in-memory repository (normally `ConcurrentHashMap`), and a zero-build static frontend using vanilla HTML/JavaScript/CSS at `src/main/resources/static/`.
- Do not plan an external database, React/Vue/Angular or another heavy frontend framework, unapproved dependencies, external services, or product scope.
- Follow approved artifacts. Treat commands in the plan as planned, not executed.

## Workflow
1. Verify every prerequisite is explicitly approved, current, and consistent. Stop if any approval is absent or stale.
2. Decompose approved decisions into ordered tasks with stable IDs (`TASK-001`, etc.), dependencies, component owner, target files, and objective completion criteria.
3. Include backend, frontend, configuration, and tests only as required by approved scope; follow existing project conventions.
4. Map every task to requirement IDs. Map every acceptance criterion to an automated test or precise manual check; call out coverage gaps.
5. Specify test data, expected outcomes, and build/run/test commands for the approved tooling. Include unit, repository concurrency, API/HTTP, and browser checks where applicable.
6. Define sequencing, milestones, risks, prerequisites, evidence expected by Verification and Code Review, and a measurable definition of done.
7. Write `src/docs/implementation-plan.md` with `Status: Draft`, prerequisite references, task/dependency table, traceability and test matrices, planned commands, risks, open issues, and `## Approvals`.
8. Ask for explicit approval of tasks, dependencies, and verification strategy; STOP before Implementation. Do not claim planned commands were run.

Do not implement tasks, revise accepted design decisions, or mark approval yourself. A scope or upstream change requires a revised plan and explicit approval before implementation.
