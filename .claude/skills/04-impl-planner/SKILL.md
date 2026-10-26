---
name: 04-impl-planner
description: "Translate approved Routine Stacker architecture into prioritized, actionable implementation tasks (P1-P4), milestones, and a verification matrix in src/docs/implementation-plan.md."
user-invocable: true
---

# Implementation Planner Skill (Phase 4)

Use this skill to decompose approved architecture and design review artifacts into structured, prioritized implementation tasks, milestones, and testing strategies for **Routine Stacker**.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`
- Approved `src/docs/design-review.md` (with `Pass` disposition)

## Output Target
- Deliverable: `src/docs/implementation-plan.md`
- Initial State: `Status: Draft`

## Scope of Skill
- Work breakdown structure with priority levels (P1: Core/Critical, P2: Essential Features, P3: Enhancements/Polish, P4: Nice-to-have).
- Task sequencing, milestones, and dependency mapping.
- Target file paths, classes, and components for each task.
- Verification strategy matrix mapping tasks and requirements to planned tests (Unit, Integration, Frontend/E2E).
- Build, test, and run commands.

## Procedure

1. **Establish Work Breakdown Structure (P1 - P4)**:
   - Break work down into bite-sized, sequential tasks with stable IDs (`TASK-001`, `TASK-002`, ...):
     - **P1 (Core Infrastructure & Domain Foundation)**:
       - Build configuration (Maven/Gradle with Spring Boot dependencies).
       - Core domain model (`Habit`, `HabitRecord`) and validation constraints.
       - In-memory repository interface and `ConcurrentHashMap` implementation.
     - **P2 (Business Logic & REST API)**:
       - Service layer (CRUD, streak calculation, completion toggle).
       - REST controllers, DTOs, validation handling, and error response advice.
       - Backend unit and MockMvc integration tests.
     - **P3 (Static Frontend & UI Integration)**:
       - Semantic HTML skeleton (`index.html`) under `src/main/resources/static/`.
       - Modern, responsive CSS styling (`style.css`).
       - Vanilla JavaScript client (`app.js`) wiring API calls, DOM manipulation, streak badges, and toast alerts.
     - **P4 (Polish, Accessibility & Verification Prep)**:
       - Keyboard navigation, ARIA attributes, and empty/loading states.
       - End-to-end smoke verification scripts.

2. **Define Task Metadata**:
   - For each task, document:
     - Task ID and Title
     - Priority (`P1`, `P2`, `P3`, `P4`)
     - Associated Requirements (`REQ-xxx`)
     - Target Files & Directories
     - Dependencies (preceding task IDs)
     - Definition of Done (acceptance criterion)

3. **Define Milestones & Execution Stages**:
   - Group tasks into logical release milestones:
     - *Milestone 1*: Backend domain and repository verified by unit tests.
     - *Milestone 2*: REST API endpoints fully operational and verified via MockMvc.
     - *Milestone 3*: Frontend UI completed, responsive, and communicating with REST endpoints.
     - *Milestone 4*: End-to-end integration and accessibility validation complete.

4. **Map Verification Strategy**:
   - Create a test matrix detailing:
     - Test category (Unit, Repository Concurrency, Spring MockMvc, Static UI/Browser)
     - Target requirement IDs verified
     - Planned test methods/classes
     - Expected assertion outcomes

5. **Generate `src/docs/implementation-plan.md`**:
   - Format:
     - Document Status & Prerequisite Traceability
     - Plan Overview & Architecture Alignment
     - Implementation Tasks Table (P1 through P4)
     - Milestones & Dependency Graph
     - Test & Verification Matrix
     - Execution Commands (Planned build/test/run commands)
     - Risk Management & Fallback Strategies
     - Approvals Section
   - Present task breakdown to user and pause for approval before Phase 5.
