---
name: 07-code-review-agent
description: "Conduct an independent, thorough code review of Routine Stacker code and verification evidence, producing src/docs/code-review.md with quality findings and readiness disposition."
user-invocable: true
---

# Code Review Skill (Phase 7)

Use this skill to perform an independent, objective review of source code, verification outcomes, architecture adherence, and release readiness for **Routine Stacker**.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`
- Approved `src/docs/design-review.md`
- Approved `src/docs/implementation-plan.md`
- Approved `src/docs/implementation.md`
- Passing `src/docs/verification.md`

## Output Target
- Deliverable: `src/docs/code-review.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Clean Code principles, SOLID, and DRY assessment.
- Error handling, validation boundaries, and exception leakage.
- Concurrency and thread safety review of in-memory collections (`ConcurrentHashMap`).
- Security assessment: XSS prevention in frontend JavaScript, input validation in backend controllers.
- Test quality and regression risk analysis.
- Review disposition and release readiness recommendation.

## Procedure

1. **Review Diff & Changed Files**:
   - Inspect all Java backend source files (`*Controller.java`, `*Service.java`, `*Repository.java`, `*Dto.java`).
   - Inspect all frontend static files (`index.html`, `style.css`, `app.js`).
   - Inspect test files (`*Test.java`).

2. **Evaluate Architecture & Code Quality**:
   - Verify strict layer separation (Controllers do not talk directly to repositories; services do not manipulate raw HTTP response entities).
   - Ensure repository thread safety: atomic operations, no non-thread-safe compound actions outside synchronized blocks or atomic updates.
   - Check error handling: no raw stack traces leaked to HTTP clients; proper `@ExceptionHandler` or `ProblemDetail`.
   - Frontend quality: no global variable leaks; proper event delegation; no `innerHTML` injection.

3. **Audit Verification Evidence**:
   - Check that verification claims in `src/docs/verification.md` are supported by real test executions.
   - Identify any untested edge cases (e.g., negative streak dates, empty names, concurrent completion updates).

4. **Document Findings with Severity**:
   - Assign finding IDs: `REV-001`, `REV-002`, etc.
   - Specify:
     - Title and Affected File/Line Reference
     - Severity: `Blocking`, `Major`, `Minor`, or `Nitpick`
     - Description of Concern
     - Recommended Remediation
     - Blocking Status

5. **Determine Review Disposition**:
   - `Pass`: No blocking or major unresolved issues; code meets quality bar.
   - `Changes Required`: Blocking findings must be resolved in Phase 5 and reverified in Phase 6.
   - `Blocked`: Architectural deviation or missing tests prevent approval.

6. **Generate `src/docs/code-review.md`**:
   - Include:
     - Review Scope & Artifact References
     - Code Quality & Clean Code Evaluation
     - Security & Concurrency Audit
     - Findings Log (table with severity, status, and remediation)
     - Test Suite Adequacy & Residual Risk Analysis
     - Overall Disposition & Release Readiness Verdict
     - Approvals Section
   - Present summary to the user and halt for approval before Phase 8.
