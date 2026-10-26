---
name: 06-verification-writer
description: "Verify Routine Stacker implementation against acceptance criteria, execute test suites, and produce reproducible test evidence in src/docs/verification.md."
user-invocable: true
---

# Verification Writer Skill (Phase 6)

Use this skill to execute automated tests, validate REST API endpoints, inspect frontend behavior, and compile comprehensive verification evidence for **Routine Stacker**.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`
- Approved `src/docs/design-review.md`
- Approved `src/docs/implementation-plan.md`
- Completed `src/docs/implementation.md` with application code

## Output Target
- Deliverable: `src/docs/verification.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Test execution and log gathering (JUnit, Maven/Gradle test runs).
- Verification of each acceptance criterion from Phase 1 (`REQ-xxx`).
- REST endpoint validation (status codes, JSON response shapes, error contracts).
- In-memory lifecycle and concurrency testing verification.
- Frontend static asset verification (DOM rendering, console error checks, responsive behavior, accessibility).
- Defect logging and pass/fail summary.

## Procedure

1. **Execute Automated Test Suite**:
   - Run the build and test runner (e.g., `./mvnw clean test` or `gradle test`).
   - Capture real command outputs, test counts, execution duration, and exit codes.
   - Do NOT fabricate or estimate test results; record actual execution evidence.

2. **Verify Acceptance Criteria (REQ-001..n)**:
   - Go through each requirement and scenario defined in `src/docs/requirements.md`.
   - Cross-reference with executed tests and record the result:
     - `Pass`: Criterion met with supporting automated/manual evidence.
     - `Fail`: Expected outcome not met; document defect with stack trace or payload.
     - `Blocked`: Environment or prerequisite prevents verification.
     - `Not Run`: Only if tool/environment is unavailable; document manual reproduction steps.

3. **Verify In-Memory & Concurrency Behavior**:
   - Verify that concurrent habit completions behave atomically.
   - Confirm that data resets cleanly upon application restart without orphaned state.

4. **Validate Frontend & Accessibility**:
   - Verify UI rendering in browser or inspect static files:
     - HTML validity and ARIA attributes for screen reader accessibility.
     - Safe DOM text insertion (no unsanitized `innerHTML`).
     - Responsive behavior on desktop and mobile viewports.

5. **Generate `src/docs/verification.md`**:
   - Structure:
     - Document Status (`Status: Draft`)
     - Verification Environment (OS, Java version, Spring Boot version, Browser)
     - Test Execution Results & Command Logs
     - Acceptance Criteria Verification Matrix (REQ, Given/When/Then, Status, Evidence)
     - REST API Endpoint Verification Table
     - Concurrency & In-Memory Storage Verification
     - Defect Log & Known Observations
     - Final Verification Verdict (`Pass`, `Fail`, or `Conditional Pass`)
     - Approvals Section
   - Present verification verdict to the user and halt for approval before Phase 7.
