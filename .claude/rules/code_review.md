---
paths:
  - "src/docs/code-review.md"
  - "src/main/java/**"
  - "src/test/**"
  - "src/main/resources/static/**"
---
# Phase 7: Code Review and Quality Assurance

## Ownership
Prerequisites: approved, current upstream artifacts and accepted passing verification. Own and edit only `src/docs/code-review.md`. Read source and tests without changing them. Do not edit code, tests, configuration, other phase documents, workflow files, or Jira issues.

## Project Constraints
- Confirm conformance to Java/Spring Boot, a thread-safe in-memory repository, and zero-build vanilla HTML/JavaScript/CSS under `src/main/resources/static/`.
- An external database or heavy framework (React, Vue, Angular, etc.) violates constraints unless an explicit user-approved scope change says otherwise.
- Do not add features, dependencies, refactors, or fixes during review. Route changes to the owning phase and require re-verification.

## Workflow
1. Verify prerequisites are explicitly approved, current, and consistent. Stop if verification is not passing or required acceptance is absent.
2. Inspect the actual diff against requirements, architecture, plan, implementation report, and verification evidence. Do not assume a reported pass proves coverage.
3. Assess correctness, API compatibility, thread safety, validation, error leakage, safe DOM handling, accessibility, maintainability, scope, and missing tests.
4. Check approved deviations and requirement-to-code/test traceability. Identify unauthorized storage, frameworks, features, dependencies, or out-of-plan changes.
5. Report findings first, ordered by severity. Each finding needs a stable ID, file/line reference where applicable, impact, actionable remediation, owner, and blocking status. Separate findings from questions and assumptions.
6. Set disposition to `Pass`, `Changes Required`, or `Blocked`. A pass requires no unresolved blockers; do not waive risks or approve on the user's behalf.
7. Write `src/docs/code-review.md` with `Status: Draft`, reviewed scope/inputs, findings, evidence, test gaps, residual risks, disposition, and `## Approvals`.
8. Request explicit human approval of release readiness and STOP before PR Creation. After fixes, require fresh verification and review.

If there are no findings, say so without claiming zero risk. Never modify reviewed code or advance automatically.
