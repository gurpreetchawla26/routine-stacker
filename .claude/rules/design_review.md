---
paths:
  - "src/docs/design-review.md"
---
# Phase 3: Design Review and Findings

## Ownership
Prerequisites: explicitly approved and current requirements and architecture. Own and edit only `src/docs/design-review.md`. Do not change upstream documents, code, tests, other phase artifacts, workflow configuration, or Jira issues.

## Project Constraints
- Review against the Java/Spring Boot backend and thread-safe in-memory repository; external databases are not permitted.
- Review against the zero-build static HTML, vanilla JavaScript, and CSS frontend in `src/main/resources/static/`; React, Vue, Angular, and other heavy frameworks are not permitted.
- Do not introduce features, dependencies, services, or design changes. Ground observations in the approved artifacts and label uncertainties.

## Workflow
1. Verify both prerequisite documents are explicitly approved, current, and consistent. Stop if any prerequisite or approval is missing.
2. Trace each requirement through architecture components, API/data contracts, and planned verification. Identify uncovered, conflicting, or ambiguous behavior.
3. Review repository concurrency and atomicity, identity generation, duplicate actions, date boundaries, validation, and error semantics.
4. Review frontend accessibility, responsiveness, safe DOM rendering, loading/empty/error states, and consistency with the approved contracts.
5. Assess security, testability, operational assumptions, design complexity, and adherence to the fixed stack and approved scope.
6. Give every finding a stable ID, severity, evidence reference, impact, actionable recommendation, owning phase, and blocking/non-blocking status. Separate observations from questions and assumptions.
7. Set disposition to `Pass`, `Changes Required`, or `Blocked`. A pass requires no unresolved blockers; only the user may accept residual risk.
8. Write `src/docs/design-review.md` with `Status: Draft`, reviewed inputs, findings, gaps, risks, disposition, and `## Approvals`. Request explicit human approval, then STOP before planning.

Route architecture changes to the Architecture owner and review them again. Do not waive findings, alter the design yourself, mark approval, or advance automatically.
