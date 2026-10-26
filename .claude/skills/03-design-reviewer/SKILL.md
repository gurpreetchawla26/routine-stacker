---
name: 03-design-reviewer
description: "Review Routine Stacker requirements and architecture for correctness, simplicity, security, and NFR compliance. Produces src/docs/design-review.md with categorized findings and disposition."
user-invocable: true
---

# Design Reviewer Skill (Phase 3)

Use this skill to perform an independent, rigorous design review evaluating requirements coverage, architectural soundness, security, and testability for **Routine Stacker**.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`

## Output Target
- Deliverable: `src/docs/design-review.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Requirement coverage analysis (no orphaned requirements; no unbacked architecture features).
- In-memory storage and concurrency analysis (race conditions, memory leak risks, collection choices).
- Non-Functional Requirement (NFR) compliance: validation, error handling, responsiveness, accessibility.
- Security and threat analysis (XSS in vanilla JS, payload injection, sanitization).
- Testability assessment (isolation of layers, mockability, test boundaries).
- Categorized findings with severity levels and overall design disposition.

## Procedure

1. **Verify Traceability & Completeness**:
   - Verify every requirement `REQ-xxx` is mapped to an API endpoint, data model, or UI element in `src/docs/architecture.md`.
   - Ensure the architecture does not introduce unauthorized scope (e.g., database drivers, JWT auth, frontend build tools/bundlers).

2. **Conduct In-Memory Storage & Concurrency Analysis**:
   - Scrutinize concurrency controls: check for atomic mutations when updating habit completion statuses or recalculating streaks.
   - Evaluate memory growth: ensure history tracking cannot lead to unbounded memory leaks in long-running processes.

3. **Assess Security & Data Validation**:
   - Check input validation: `@NotBlank`, `@Size`, valid date ranges on REST controllers.
   - Frontend security: inspect DOM update patterns to guarantee safe rendering (`textContent`, template elements) without raw `innerHTML`.

4. **Evaluate Testability & Simplicity**:
   - Confirm business logic is decoupled from HTTP concerns so unit tests can run without starting full Spring contexts.
   - Ensure repository interface allows deterministic test fixtures.

5. **Classify Review Findings**:
   - Document each finding with:
     - Finding ID (e.g., `FIND-001`, `FIND-002`)
     - Title and description
     - Affected component and reference link
     - Severity: `Blocking`, `Major`, `Minor`, or `Informational`
     - Impact & Remediation recommendation
     - Resolution status

6. **Determine Overall Disposition**:
   - Set disposition to:
     - `Pass`: No blocking issues; architecture ready for implementation planning.
     - `Changes Required`: Blocking issues identified; must cycle back to Architecture/Requirements.
     - `Blocked`: Missing prerequisites or severe scope violations.

7. **Generate `src/docs/design-review.md`**:
   - Include: Review Scope, Traceability Audit, NFR & Security Analysis, Findings Table, Disposition Verdict, and Approvals section.
   - Halt for explicit human approval before moving to Phase 4.
