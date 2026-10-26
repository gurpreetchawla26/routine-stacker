---
paths:
  - "src/docs/verification.md"
  - "src/test/**"
---
# Phase 6: Verification and Test Execution

## Ownership
Prerequisites: approved, current upstream artifacts and accepted implementation documented in `src/docs/implementation.md`. Own and edit only `src/docs/verification.md`. You may inspect source/tests and execute checks, but must not modify application code, tests, configuration, or other phase documents.

## Project Constraints
- Verify the Java/Spring Boot application, thread-safe in-memory repository, and zero-build vanilla HTML/JavaScript/CSS frontend.
- No external database or heavy frontend framework such as React, Vue, or Angular is allowed by the approved design.
- Do not add features, dependencies, tests, or code changes while verifying. Route product defects to Implementation through the orchestrator.

## Workflow
1. Confirm required artifacts and approvals are present, current, and consistent. Stop if a prerequisite or implementation acceptance is missing.
2. Extract the approved plan's acceptance/test matrix. Inspect relevant code and tests read-only; do not modify them.
3. Run applicable approved build and test commands. Record exact command, environment/runtime version, date, exit code, outcome, and relevant output or failure evidence.
4. Verify API behavior, validation, errors, in-memory lifecycle/concurrency, and each acceptance criterion using reproducible checks.
5. Exercise the frontend in a browser when tools are available, checking API interaction, console errors, accessibility, loading/empty/error states, and desktop/mobile layout. Otherwise mark checks `Not Run` and provide manual steps.
6. Map each criterion to `Pass`, `Fail`, `Blocked`, or `Not Run`. Separate product defects from environment blockers. Any failure or required check not run prevents an overall pass unless the user explicitly approves removing that criterion.
7. Write `src/docs/verification.md` with `Status: Draft`, inputs, environment, commands/evidence, traceability matrix, defects, limitations, verdict, and `## Approvals`.
8. Request explicit human acceptance of the evidence and STOP before Code Review. Return failures to Implementation; do not repair them yourself.

Never fabricate evidence, infer browser success from backend tests, mark approval yourself, or advance automatically.
