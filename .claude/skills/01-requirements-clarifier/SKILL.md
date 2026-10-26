---
name: 01-requirements-clarifier
description: "Elicit, structure, and clarify requirements for Zenith Habit Tracker. Generates scope, assumptions, functional/non-functional requirements, and Given/When/Then acceptance criteria in src/docs/requirements.md."
user-invocable: true
---

# Requirements Clarifier Skill (Phase 1)

Use this skill to guide the requirements discovery, clarification, and formalization process for the **Zenith Habit Tracker** project.

## Output Target
- Deliverable: `src/docs/requirements.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Target Stack: Spring Boot backend, thread-safe in-memory data store, vanilla JavaScript/CSS static frontend.
- Establish boundaries: in-scope vs. out-of-scope capabilities.
- Define functional requirements (FRs) and non-functional requirements (NFRs).
- Write testable user stories with Gherkin-style Given/When/Then acceptance criteria.
- Catalog assumptions, constraints, and open questions.

## Procedure

1. **Clarify Project Scope & Context**:
   - Determine core habit tracking features: habit creation, frequency/cadence (daily/weekly), completion check-ins, history log, streak counting, and visual progress.
   - Clarify boundaries: explicit reminder that data is ephemeral (in-memory; lost on application restart). No external database, authentication, or complex multi-tenant logic.

2. **Formulate Functional Requirements (FRs)**:
   - Assign stable identifiers: `REQ-001`, `REQ-002`, etc.
   - For each requirement, detail:
     - User Story (`As a [role], I want to [action], so that [value]`)
     - Preconditions and inputs
     - Expected system behavior and outputs

3. **Formulate Non-Functional Requirements (NFRs)**:
   - Performance: sub-second local REST responses.
   - Concurrency: thread-safe operations on in-memory collections (`ConcurrentHashMap`).
   - Usability & Accessibility: responsive viewport (mobile and desktop), WCAG 2.1 AA compliant contrast and keyboard accessibility.
   - Resiliency & Data Validation: sensible defaults, clear error messages for invalid payloads.

4. **Author Given/When/Then Acceptance Criteria**:
   - For each requirement, provide concrete scenario definitions:
     - `Scenario: Successfully create a new habit`
       - `Given` a user submits valid habit payload with name and daily frequency
       - `When` the habit creation request is processed
       - `Then` the habit is stored, assigned a unique ID, and returns HTTP 201 Created
     - `Scenario: Edge cases & validation errors` (e.g., blank title, duplicate names, invalid dates).

5. **Generate `src/docs/requirements.md`**:
   - Organize document with headers:
     - Document Metadata (`Status: Draft`, Author, Date, Version)
     - Executive Summary & Personas
     - Scope & Boundaries (In-Scope vs. Out-of-Scope)
     - Functional Requirements (REQ-001..n)
     - Non-Functional Requirements (NFR-001..n)
     - Acceptance Criteria Matrix (REQ vs. Scenarios)
     - Constraints, Risks & Assumptions
     - Open Questions & Decision Log
     - Approvals Section
   - Present summary to the user and halt for explicit approval before proceeding to Phase 2.
