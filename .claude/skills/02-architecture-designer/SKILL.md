---
name: 02-architecture-designer
description: "Architect Spring Boot REST API, thread-safe in-memory storage, and static vanilla JS/CSS frontend for Zenith Habit Tracker. Generates src/docs/architecture.md with component models and Mermaid diagrams."
user-invocable: true
---

# Architecture Designer Skill (Phase 2)

Use this skill to design the technical architecture, data model, API specifications, and system interactions for **Zenith Habit Tracker**.

## Prerequisites
- Approved `src/docs/requirements.md`

## Output Target
- Deliverable: `src/docs/architecture.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Component decomposition (Layered Architecture: Controller, Service, In-Memory Repository).
- REST API design & contract definitions (endpoints, HTTP verbs, payload schemas, response codes).
- Domain and DTO models.
- Concurrency, thread safety, and memory management model (`ConcurrentHashMap`, atomic ID generation).
- Static frontend architecture (vanilla ES6+, semantic HTML5, CSS custom properties, safe DOM manipulation).
- Visual system and data flow diagrams using Mermaid.

## Procedure

1. **Map Requirements to Architectural Components**:
   - Trace each `REQ-xxx` from `src/docs/requirements.md` to specific architectural layers:
     - Presentation: Static assets served from `src/main/resources/static/`
     - Controller: REST endpoints handling serialization, status codes, and input validation.
     - Service: Business logic (streak calculations, completion validation, filtering).
     - Repository: In-memory store implementing clear repository interfaces.

2. **Define In-Memory Storage Strategy**:
   - Use thread-safe data structures (`ConcurrentHashMap<UUID, Habit>`, `AtomicLong`, or `CopyOnWriteArrayList`).
   - Detail identity generation strategies (UUID vs. auto-incrementing longs).
   - Document data lifecycle and caveats (ephemeral in-memory lifecycle resets upon JVM restart).

3. **Specify REST API Contracts**:
   - Document URI routes, methods (`GET`, `POST`, `PUT`, `DELETE`), request bodies, response payloads, and HTTP status codes (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`).
   - Standardize error responses (e.g., RFC 7807 `ProblemDetail` or custom JSON error envelope).

4. **Design Static Frontend Architecture**:
   - Layout: Responsive, modern UI using vanilla CSS variables, flexbox, and grid.
   - Scripting: Vanilla ES6 modules or single-script file communicating via `fetch()`.
   - Security: Safe DOM manipulation (`textContent` instead of raw `innerHTML`) to prevent XSS.
   - State Handling: Client-side model sync with backend; loading indicators, empty states, and error alerts.

5. **Generate Mermaid Diagrams**:
   - Architecture component diagram:
     ```mermaid
     graph TD
         Client[Browser Frontend: HTML/CSS/JS] -->|HTTP REST / JSON| Controller[Spring Boot REST Controller]
         Controller --> Service[Habit Service]
         Service --> Repo[In-Memory Habit Repository]
         Repo --> Store[(ConcurrentHashMap Store)]
     ```
   - Data flow and sequence diagrams for key operations (e.g., creating a habit, recording daily completion).

6. **Generate `src/docs/architecture.md`**:
   - Document structure:
     - Architecture Overview & System Context
     - Technology Stack & Version Matrix (Java 17+, Spring Boot 3.x, vanilla frontend)
     - Architectural Decision Records (ADRs)
     - Data Models & In-Memory Storage Strategy
     - REST API Contract & Error Handling Specifications
     - Static Frontend Architecture & UX Patterns
     - Mermaid Architecture & Sequence Diagrams
     - Requirement Traceability Matrix (REQ to Component/Endpoint)
     - Risk Assessment & Approvals
   - Present summary to the user and halt for approval before Phase 3.
