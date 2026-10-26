# Routine Stacker: Implementation Report

## Document Metadata

| Field | Value |
|---|---|
| Status | Draft |
| Phase | 5 of 6: Implementation (state file phase `implementation`) |
| Date | 2026-10-26 |
| Version | 0.1 |
| Inputs | `docs/requirements.md` v0.2 (approved 2026-10-26T08:21:18Z); `docs/architecture.md` v0.2 (approved per phase gate); `docs/impl-plan.md` v0.2 (Ready for Review; approval not yet recorded) |

## 1. Executive Summary

The backend, static frontend, and test code for TASK-001 to TASK-013 were written to the approved plan. **No build or test command has been executed.** The environment has no Java or Maven installed (`java` and `mvn` both "command not found"), so the code has been checked by reading only. TASK-014 (manual browser checks) has not been done.

## 2. Task Status

| Task | Status | Notes |
|---|---|---|
| TASK-001 Maven scaffold | Written | `pom.xml` pins Spring Boot `3.3.5`. This version was not checked against the current release list. Confirm before the first build. `server.address=localhost` in `application.properties`. |
| TASK-002 Domain model | Written | `Habit`, `Frequency`. The planned `CheckIn` record was not created: history returns `LocalDate` values, so no type is needed (see section 5). |
| TASK-003 Clock | Written | `ClockConfig` with `Clock.systemDefaultZone()`. |
| TASK-004 Repository | Written | `HabitRepository` interface and `InMemoryHabitRepository` on `ConcurrentHashMap`, `AtomicLong`, and concurrent key sets. |
| TASK-005 Streak logic | Written | `StreakCalculator`, with unit tests in `StreakCalculatorTest` covering REQ-006 and the Monday week boundary. |
| TASK-006 Habit service | Written | `HabitService`: trim, validation, reserve-then-store uniqueness, response assembly. |
| TASK-007 Check-in service | Written | `CheckInService`: check-in, same-day undo, newest-first history, 404 for unknown habits. |
| TASK-008 Habit controller | Written | `HabitController`, returns 201 with `Location`. |
| TASK-009 Check-in controller | Written | `CheckInController`, returns 201 or 200 for check-in and 204 for undo. |
| TASK-010 Error handling | Written | `GlobalExceptionHandler` and `ErrorResponse`. |
| TASK-011 Static UI | Written | `index.html`, `app.js`, `styles.css`. Uses `textContent` only. No framework or build step. |
| TASK-012 API and timing tests | Written | `HabitApiTest` (MockMvc, all endpoints, error shape, localhost binding). `LatencyTest` (JUnit timing, p95 at 1,000 habits and 100,000 check-ins). |
| TASK-013 Concurrency tests | Written | `InMemoryHabitRepositoryConcurrencyTest`: 20 simultaneous creates, 50 simultaneous check-ins, 50 mixed operations. |
| TASK-014 Manual browser checks | Not started | Requires a running application, so it depends on the build. |

## 3. Changed Files

| Path | Purpose |
|---|---|
| `pom.xml` | Maven build, Java 17, Spring Boot 3.3.5 |
| `src/main/resources/application.properties` | `server.address=localhost`, `server.port=8080` |
| `src/main/java/com/routinestacker/RoutineStackerApplication.java` | Entry point |
| `src/main/java/com/routinestacker/config/ClockConfig.java` | Clock bean |
| `src/main/java/com/routinestacker/domain/{Habit,Frequency}.java` | Domain types |
| `src/main/java/com/routinestacker/repository/{HabitRepository,InMemoryHabitRepository}.java` | Storage |
| `src/main/java/com/routinestacker/service/{HabitService,CheckInService,StreakCalculator}.java` | Rules |
| `src/main/java/com/routinestacker/controller/{HabitController,CheckInController}.java` | REST API |
| `src/main/java/com/routinestacker/dto/{CreateHabitRequest,HabitResponse,CheckInResponse}.java` | Payloads |
| `src/main/java/com/routinestacker/web/*.java` | Error shape, exceptions, global handler |
| `src/main/resources/static/{index.html,app.js,styles.css}` | Frontend |
| `src/test/java/com/routinestacker/support/FixedClockConfig.java` | Fixed test clock (2026-10-26, UTC) |
| `src/test/java/com/routinestacker/service/StreakCalculatorTest.java` | REQ-006 unit tests |
| `src/test/java/com/routinestacker/api/HabitApiTest.java` | HTTP tests |
| `src/test/java/com/routinestacker/api/LatencyTest.java` | NFR-001 timing |
| `src/test/java/com/routinestacker/repository/InMemoryHabitRepositoryConcurrencyTest.java` | NFR-002 concurrency |
| `src/docs/implementation.md` | This report |

## 4. Requirement Traceability (code and planned tests)

| Requirement | Code | Test (written, not run) |
|---|---|---|
| REQ-001 Create | `HabitService.create`, `HabitController.create` | `HabitApiTest` create, validation, and duplicate tests; concurrency test |
| REQ-002 List | `HabitService.list`, `InMemoryHabitRepository.findAll` | `HabitApiTest.listIsEmptyWhenNoHabitsExist` |
| REQ-003 Check in | `CheckInService.checkIn` | `HabitApiTest` check-in tests; concurrency test |
| REQ-004 Undo | `CheckInService.undoToday` | `HabitApiTest` undo tests |
| REQ-005 History | `CheckInService.history` | Newest-first order not covered by an API test (see section 5) |
| REQ-006 Streak | `StreakCalculator` | `StreakCalculatorTest` (all scenarios); `HabitApiTest` streak after check-in |
| REQ-007 Responsive UI | `static/` | Manual only (TASK-014) |
| NFR-001 Performance | Service layer | `LatencyTest` |
| NFR-002 Concurrency | `InMemoryHabitRepository` | `InMemoryHabitRepositoryConcurrencyTest` |
| NFR-003 Validation | `HabitService.requireText`, `parseFrequency` | `HabitApiTest` validation tests |
| NFR-004 Error shape | `GlobalExceptionHandler`, `ErrorResponse` | `HabitApiTest` error-shape assertions |
| NFR-005 to NFR-007 | `static/` | Manual only (TASK-014) |
| NFR-008 Technology | `pom.xml`, `static/` | Build (not run) |
| NFR-009 Durability | In-memory only; UI note in `index.html` | Restart check is manual (TASK-014) |

## 5. Deviations from the Approved Plan

1. **No `CheckIn` record.** Architecture section 5.1 lists a `CheckIn` entity. History returns `LocalDate` values directly, so the record would be unused. This does not change any contract.
2. **Unmapped framework errors.** `GlobalExceptionHandler` covers the application's own errors and malformed bodies. Framework errors without a handler (for example, 405 for an unsupported method) fall back to Spring Boot's default error body. NFR-004 says every 4xx and 5xx should use the `ErrorResponse` shape, so this is a gap. Needs a decision: add a catch-all handler, or accept the gap.
3. **Newest-first history is not covered by an API test.** Check-ins can only be created for today through the API, so the test cannot seed past dates. The ordering logic is in `CheckInService.history` but has no test yet. Add a service-level test that seeds the repository.
4. **Spring Boot version.** The plan says "latest stable 3.3.x patch". `3.3.5` was written without checking the release list.
5. **Implementation report location.** The agent definition puts this report in `src/docs/`. The earlier phase artifacts are in `docs/`. This file follows the agent definition. The gate only requires `src/`.
6. **Minor.** `LatencyTest` has an unused import (`Habit`). This compiles but should be removed.

## 6. Checks Run

| Check | Command | Result |
|---|---|---|
| Java runtime present | `java -version` | **Not available.** `java: command not found`. |
| Maven present | `mvn -v` | **Not available.** `mvn: command not found`. |
| Build and tests | `mvn -q verify` | **Not run.** No Maven. |
| Application start | `mvn spring-boot:run` | **Not run.** |
| Manual browser checks | TASK-014 | **Not run.** |

Code was reviewed by reading only. Compilation and test outcomes are unknown.

## 7. Known Limitations

- Nothing has been compiled or executed. Expect at least some compile or test fixes once the code is built.
- The latency check depends on machine load. The p95 threshold is a guard, not a benchmark.
- The browser UI has not been checked for layout, accessibility, or behavior.

## 8. Next Steps (for the user)

1. Install JDK 17 and Maven, then run `mvn -q verify` from the project root.
2. Confirm the Spring Boot 3.3.x patch in `pom.xml`.
3. Run `mvn spring-boot:run` and complete the TASK-014 manual checks.
4. Decide deviation 2 (framework error shape) and add the newest-first history test (deviation 3).

## 9. Update: Build Run and Code Review Follow-up (2026-10-26)

Sections 1, 6, and 7 describe the state before any build. Current state:

- **Build run:** `mvn -B clean verify` passes, 30 of 30 tests, exit 0 (latest run). The earlier 29-test result is superseded.
- **Fixes applied:** `server.address` set to `localhost` in `application.properties` (deviation: D-3 from the verification report). Added `simultaneousCheckInsOnDistinctDaysKeepEveryCheckIn` to `InMemoryHabitRepositoryConcurrencyTest`: 50 threads add 50 distinct dates and assert an exact count of 50. This addresses finding 1 below.
- **Live checks:** still not run. The JVM cannot open a loopback selector on this machine (waiver B-1).

Advisory findings from the read-only code review. None have been fixed except finding 1. Owner for all: Implementation.

| # | Severity | Location | Finding | Status |
|---|---|---|---|---|
| 1 | Major (test quality, NFR-002) | `InMemoryHabitRepositoryConcurrencyTest` | Mixed undo/check-in test only checks ranges and size ≤ 10; dropping `addCheckIn` would still pass | Fixed (exact-count test added) |
| 2 | Minor (NFR-007) | `index.html:41`, `app.js:145` | Create-form errors appear in the "Your stack" section, far from the form | Fixed: `index.html` adds `#form-error` inside the create form, next to the submit button; create errors go there, and list-action errors stay in `#error`. JS syntax checked; build: 33 of 33 pass. Not run in a browser (waiver B-1). |
| 3 | Minor (NFR-005) | `app.js:108`, `app.js:51` | Focus is lost after each check-in or undo because the list is re-rendered | Fixed: toggles carry `data-habit-id`; `runAction` calls `focusToggle` after reload. JS syntax checked with `node --check`; build: 33 of 33 pass. Keyboard behaviour not run in a browser (waiver B-1). |
| 4 | Minor (NFR-005, WCAG 1.4.11) | `styles.css:12` | `--border` `#9ca3af` on white is about 2.5:1, below 3:1 (calculated, not measured) | Fixed: `--border` changed to `#6b7280`, 4.83:1 on white and 4.39:1 on the page background (calculated). Build: 33 of 33 pass. Browser check not run (waiver B-1). |
| 5 | Minor (security) | `CheckInController.java:28, 35` | No CSRF, Origin, or Host checks on state-changing endpoints; low impact for a local in-memory tool | Open (architecture decision) |
| 6 | Minor (NFR-001) | `LatencyTest.java:53-56`, `HabitService.java:54-56` | Latency test does not time the list path, the heaviest path at 100k check-ins | Open |
| 7 | Minor (test gap) | `HabitApiTest.java:43-45` | `serverBindsToLocalhostOnly` reads the configured property, not the actual bound address | Open |
| 8 | Minor (test gap, REQ-002, REQ-005) | `CheckInService.java:42-44` | Newest-first ordering and creation-order listing have no API test (see deviation 3) | Fixed: `HabitApiTest.listReturnsHabitsInCreationOrder` (REQ-002) and `CheckInServiceTest.historyIsNewestFirstRegardlessOfInsertionOrder` (REQ-005, seeds past dates through the repository). Build: 35 of 35 pass. |
| 9 | Minor (extends D-1) | `GlobalExceptionHandler.java:18-46` | No catch-all handler; unhandled 500s and framework errors fall outside the NFR-004 shape | Fixed: `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`, with a catch-all 500 handler. Tests added for 405, 415, and unknown-route 404. Build: 33 of 33 pass. |

Verified by reading (no finding): streak logic, check-in idempotence, atomic name reservation, validation before state changes, and no XSS (all text goes through `textContent`).

Open questions: the weekly streak rule comes from design OQ-3 and is not in the requirements; it needs confirmation.

## Approvals

| Field | Value |
|---|---|
| Approved | No |
| Approved by | — |
| Approval date | — |
| Scope approved | — |
| Notes | Implementation is drafted. Build, tests, and browser checks have not run. Human acceptance is needed before Verification. |

> Approvals are recorded only after the user explicitly confirms them. Verification must not begin on this implementation until it is accepted.
