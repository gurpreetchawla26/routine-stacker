# Routine Stacker: Implementation Plan

## Document Metadata

| Field | Value |
|---|---|
| Status | Ready for Review |
| Phase | 3 of 6: Implementation Planner (state file phase `implementation-planner`) |
| Author | Implementation Planner (AI-drafted for user review) |
| Date | 2026-10-26 |
| Version | 0.2 |
| Prerequisites | `docs/requirements.md` v0.2, approved 2026-10-26T08:21:18Z. `docs/architecture.md` v0.2, approved per the phase gate. |

**Planned, not executed.** Every command in this document is planned. None has been run, and no result here is execution evidence.

**Prerequisite decision.** The planner agent definition mentions a design review. The user decided (OQ-P1) to proceed without a separate design review, because the HITL phase gate supersedes it.

## 1. Scope of This Plan

Implements only the approved requirements REQ-001 to REQ-007 and NFR-001 to NFR-009, using the approved architecture (Java 17, Spring Boot 3.3.x, Maven, package `com.routinestacker`, server bound to `localhost`).

Out of scope, per requirements: authentication, persistence, edit and delete, external services, heavy frontend frameworks.

## 2. Task Breakdown

Stable IDs. Each task lists its dependencies, target paths (under the project root, using Maven layout), and objective completion criteria.

| ID | Task | Depends on | Target paths | Completion criteria |
|---|---|---|---|---|
| TASK-001 | Scaffold Maven project: Java 17, latest stable Spring Boot 3.3.x patch as the parent, web, validation, and test starters. Set `server.address=localhost`. | None | `pom.xml`, `src/main/java/com/routinestacker/RoutineStackerApplication.java`, `src/main/resources/application.properties` | `mvn -q verify` passes with an empty context-load test. Server binds to `localhost` (verified by test, TASK-012). |
| TASK-002 | Domain model: `Habit` (immutable record with `sequence`), `Frequency` enum, `CheckIn` value. | TASK-001 | `domain/Habit.java`, `domain/Frequency.java`, `domain/CheckIn.java` | Compiles. `Habit` fields match architecture section 5.1. `Frequency` has exactly `DAILY` and `WEEKLY`. |
| TASK-003 | `ClockConfig` providing `Clock.systemDefaultZone()`. | TASK-001 | `config/ClockConfig.java` | Injectable `Clock` bean. Replaceable in tests. |
| TASK-004 | `HabitRepository` interface and `InMemoryHabitRepository` with `habits`, `nameIndex`, `checkIns`, and `AtomicLong` sequence. Name reservation uses `putIfAbsent`. Check-in add and remove use concurrent sets. | TASK-002 | `repository/HabitRepository.java`, `repository/InMemoryHabitRepository.java` | Create returns conflict on duplicate normalized name. Check-in add returns `true` once per date. Remove is idempotent. Sorted listing by `sequence`. |
| TASK-005 | `StreakCalculator`: pure function for DAILY and WEEKLY (ISO weeks, Monday start). Unit tests for every REQ-006 scenario and day/week boundary. | TASK-002 | `service/StreakCalculator.java`, `src/test/java/com/routinestacker/service/StreakCalculatorTest.java` | All REQ-006 scenarios pass with fixed dates. Boundary cases (midnight, week change) covered. |
| TASK-006 | `HabitService`: trim and normalize input, validate (NFR-003), create (409 on duplicate), list, get by ID with `streak` and `checkedInToday`. | TASK-003, TASK-004, TASK-005 | `service/HabitService.java`, `dto/CreateHabitRequest.java`, `dto/HabitResponse.java`, `web/*Exception.java` | Validation rules in architecture section 5.4 enforced before any write. Response contains all fields from section 5.3. |
| TASK-007 | `CheckInService`: check-in (201 new, 200 duplicate), undo today (204), history newest first. Unknown habit throws not-found. | TASK-004, TASK-006 | `service/CheckInService.java` | Behaves per REQ-003 to REQ-005 and OQ-A1 (404 for unknown habit). |
| TASK-008 | `HabitController`: `POST /api/habits` (201 with `Location`), `GET /api/habits`, `GET /api/habits/{id}`. | TASK-006 | `controller/HabitController.java` | Status codes match architecture section 5.3. |
| TASK-009 | `CheckInController`: `POST /api/habits/{id}/check-ins`, `DELETE /api/habits/{id}/check-ins/today`, `GET /api/habits/{id}/check-ins`. | TASK-007 | `controller/CheckInController.java` | Status codes match architecture section 5.3. |
| TASK-010 | `GlobalExceptionHandler` and `ErrorResponse` record in NFR-004 shape. Malformed JSON and unknown enum values map to 400. No stack traces. | TASK-008 | `web/GlobalExceptionHandler.java`, `web/ErrorResponse.java` | Every 4xx and 5xx response has the exact five fields. |
| TASK-011 | Static UI: semantic HTML form and list, vanilla JS fetch with loading, empty, and error states, `textContent` only, responsive CSS, visible focus, NFR-009 durability note. | TASK-009 | `static/index.html`, `static/app.js`, `static/styles.css` | Matches architecture section 7. No framework, no build step. |
| TASK-012 | API tests with `MockMvc`: every endpoint, status code, error shape, and the `localhost` binding check. Plus a simple JUnit timing check for NFR-001 (p95 ≤ 500 ms on the NFR-001 load, measured in-process with `System.nanoTime`). | TASK-010 | `src/test/java/com/routinestacker/api/*Test.java`, `src/test/java/com/routinestacker/api/LatencyTest.java` | Every acceptance scenario in REQ-001 to REQ-006 and NFR-004 has a passing test. Timing check passes at 1,000 habits and 100,000 check-ins. |
| TASK-013 | Repository concurrency tests: 20 simultaneous creates with one name; 50 threads of mixed create, check-in, and undo. | TASK-004 | `src/test/java/com/routinestacker/repository/InMemoryHabitRepositoryConcurrencyTest.java` | Exactly one 201 and 19 conflicts. Final state consistent with successful operations (NFR-002). |
| TASK-014 | Manual browser checks for REQ-007 and NFR-005, NFR-006: 360 px and 1920 px layouts, keyboard-only flow, focus visibility, contrast. No browser-test dependency is added. | TASK-011 | Evidence recorded under `docs/evidence/` (manual record) | Each check recorded with date, viewport, and result. |

## 3. Milestones

| Milestone | Tasks | Done when |
|---|---|---|
| M1: Backend core | TASK-001 to TASK-007, TASK-013 | Core services and repository pass unit and concurrency tests. |
| M2: HTTP API | TASK-008 to TASK-010, TASK-012 | All API tests and the timing check pass. Error shape verified. |
| M3: UI and browser checks | TASK-011, TASK-014 | UI works against the API. Manual browser checks recorded. |

Sequencing: M1, then M2, then M3. TASK-013 can run in parallel with TASK-006 and later.

## 4. Requirement Traceability

| Requirement | Tasks |
|---|---|
| REQ-001 Create habit | TASK-002, TASK-004, TASK-006, TASK-008, TASK-010 |
| REQ-002 List habits | TASK-004, TASK-006, TASK-008 |
| REQ-003 Check in today | TASK-004, TASK-007, TASK-009 |
| REQ-004 Undo today | TASK-004, TASK-007, TASK-009 |
| REQ-005 History | TASK-004, TASK-007, TASK-009 |
| REQ-006 Streak | TASK-005, TASK-006 |
| REQ-007 Responsive interface | TASK-011, TASK-014 |
| NFR-001 Performance | TASK-012 (JUnit timing check, OQ-P2) |
| NFR-002 Concurrency | TASK-004, TASK-013 |
| NFR-003 Validation | TASK-006, TASK-012 |
| NFR-004 Error shape | TASK-010, TASK-012 |
| NFR-005 Accessibility | TASK-011, TASK-014 |
| NFR-006 Responsiveness | TASK-011, TASK-014 |
| NFR-007 Usability | TASK-011 |
| NFR-008 Technology | TASK-001, TASK-011 |
| NFR-009 Durability | TASK-011 (UI note), restart check in TASK-014 |

## 5. Acceptance Criteria to Test Matrix

| Criterion (requirements section 5) | Verification | Test or check |
|---|---|---|
| REQ-001: 7 scenarios | Automated | TASK-012 `HabitCreateApiTest`, including the 20-thread case in TASK-013 |
| REQ-002: 2 scenarios | Automated | TASK-012 `HabitListApiTest` |
| REQ-003: 3 scenarios | Automated | TASK-012 `CheckInApiTest` |
| REQ-004: 2 scenarios | Automated | TASK-012 `CheckInApiTest` |
| REQ-005: 2 scenarios | Automated | TASK-012 `CheckInHistoryApiTest` |
| REQ-006: 4 scenarios | Automated | TASK-005 `StreakCalculatorTest` and TASK-012 `StreakApiTest` |
| REQ-007: 3 scenarios | Manual | TASK-014 (360 px, 1920 px, keyboard-only). Recorded under `docs/evidence/`. |

Coverage notes: REQ-007 is covered by a manual check only (OQ-P4). NFR-001 is covered by a JUnit timing check, not a dedicated benchmarking tool (OQ-P2).

## 6. Test Data

- Fixed `Clock` (for example, `2026-10-26T08:00:00Z` in UTC) for all date-dependent tests.
- Dates in REQ-005 and REQ-006 scenarios use explicit `LocalDate` values, not `now()`.
- Concurrency tests use a fixed-size thread pool and a `CountDownLatch` so all requests start together.
- Timing check seeds 1,000 habits and 100,000 check-ins before measuring.
- Long-name test uses a 101-character string of `a`.

## 7. Planned Commands (not executed)

```bash
mvn -q test                 # unit, API, and timing tests (TASK-005, TASK-012)
mvn -q verify              # full build including concurrency tests (TASK-013)
mvn spring-boot:run        # start the app on localhost (TASK-011, TASK-014)
```

Planned checks for the running app: `curl -i http://localhost:8080/api/habits`, and the same against other hosts to confirm they are refused (planned, not executed).

## 8. Definition of Done

- All tasks TASK-001 to TASK-014 complete, with evidence.
- `mvn -q verify` passes with no skipped tests.
- Every row in section 5 has a passing test or a recorded manual result.
- No file outside the planned target paths changed, except where a later revised plan is approved.
- Code review and verification have independent evidence available (test reports, manual browser check records).

## 9. Risks

| ID | Risk | Mitigation |
|---|---|---|
| R-P1 | Spring Boot 3.3.x patch choice affects test dependencies. | Pin the latest stable 3.3.x patch in `pom.xml` during TASK-001 and record it. |
| R-P2 | Manual browser checks are hard to reproduce. | Record date, viewport, steps, and result for each check. |
| R-P3 | Name reservation visible briefly before storage (architecture section 4.3). | TASK-004 test: a failed lookup returns 404, not a partial object. |
| R-P4 | JUnit timing checks are sensitive to machine load and can be flaky. | Warm up before measuring, use p95 over many requests, and record the machine spec with the result. |

## 10. Open Questions (Resolved)

| ID | Question | Decision |
|---|---|---|
| OQ-P1 | Is a separate design review required before implementation? | No. Proceed without it. The HITL phase gate supersedes it. |
| OQ-P2 | How is NFR-001 checked? | Simple JUnit timing checks, no specialized benchmarking tools (TASK-012). |
| OQ-P3 | Which Spring Boot version? | Latest stable Spring Boot 3.3.x patch, pinned in `pom.xml` (TASK-001). |
| OQ-P4 | How is browser and UI validation done? | Manual verification, with no automated browser-test dependencies (TASK-014). |

## 11. Approvals

| Field | Value |
|---|---|
| Approved | No |
| Approved by | — |
| Approval date | — |
| Scope approved | — |
| Notes | Ready for review. Approval is recorded through `phase_gate.py review` after the user confirms it. Implementation has not started. |

> Approvals are recorded only after the user explicitly confirms them. Any change to the requirements or architecture after approval makes this plan stale until reapproved.
