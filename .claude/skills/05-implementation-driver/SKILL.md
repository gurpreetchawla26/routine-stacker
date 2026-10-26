---
name: 05-implementation-driver
description: "Implement Spring Boot backend, thread-safe in-memory repository, and vanilla JavaScript/CSS static frontend for Routine Stacker, documenting progress in src/docs/implementation.md."
user-invocable: true
---

# Implementation Driver Skill (Phase 5)

Use this skill to guide and execute the code implementation of **Routine Stacker** based strictly on the approved implementation plan.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`
- Approved `src/docs/design-review.md`
- Approved `src/docs/implementation-plan.md`

## Output Target
- Deliverables: Application code + `src/docs/implementation.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Java / Spring Boot backend application implementation:
  - Domain models and validation annotations
  - In-memory repository with thread-safe data structures (`ConcurrentHashMap`)
  - Service layer logic limited to capabilities in the approved requirements and plan
  - REST controllers, request/response DTOs, global exception handler (`@RestControllerAdvice`)
- Static frontend assets under `src/main/resources/static/`:
  - `index.html` (semantic HTML5, accessible structure)
  - `style.css` (modern, responsive CSS3 using flexbox/grid)
  - `app.js` (vanilla ES6+, fetch API, safe DOM rendering via `textContent`)
- Automated tests:
  - Unit tests for services and approved business rules
  - Concurrency tests for repository operations
  - Spring Boot MockMvc tests for REST endpoints and validation errors
- Generation of the implementation report in `src/docs/implementation.md`.

## Procedure

1. **Follow Planned Task Sequencing**:
   - Work through tasks according to the order defined in `src/docs/implementation-plan.md` (P1 -> P2 -> P3 -> P4).
   - Do not add unapproved features or frameworks.

2. **Implement Backend**:
   - Model layer: Immutable or well-encapsulated models with Bean Validation (`@NotBlank`, `@NotNull`, `@Min`).
   - Repository: Interface-driven in-memory store using `ConcurrentHashMap` with thread-safe atomic updates (`computeIfPresent`, `putIfAbsent`).
   - Service: Clear business methods only for capabilities defined in the approved requirements and implementation plan.
   - Controller: Clean RESTful endpoints adhering to approved HTTP verbs and response codes.

3. **Implement Static Frontend**:
   - Create `src/main/resources/static/index.html` with clean semantic elements (`<header>`, `<main>`, `<section>`, `<form>`).
   - Create `src/main/resources/static/style.css` with clean layout, responsive breakpoints, accessible focus rings, and and only the UI elements defined in the approved architecture.
   - Create `src/main/resources/static/app.js` containing asynchronous API fetch calls, state management, and XSS-safe DOM node creation.

4. **Implement Automated Tests**:
   - Place unit and integration tests under `src/test/java/`.
   - Ensure high test coverage for happy paths, validation failures, edge cases, and thread safety.

5. **Generate `src/docs/implementation.md`**:
   - Record:
     - Document Status (`Status: Draft`)
     - Task Completion Status (table mapping each `TASK-xxx` to status `Completed`)
     - Files Created and Modified (file links and descriptions)
     - Verification Summary (test commands run, pass/fail counts)
     - Key Technical Decisions & Deviations (if any, with justifications)
     - Known Limitations & In-Memory Data Lifecycle Notes
     - Approvals Section
   - Halt and present results to the user for acceptance before proceeding to Phase 6.
