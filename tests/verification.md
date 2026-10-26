# Routine Stacker: Verification Report

## Document Metadata

| Field | Value |
|---|---|
| Status | Draft |
| Phase | Verification (state file phase `verification`, artifact `tests/`) |
| Date | 2026-10-26 |
| Version | 0.2 |
| Inputs | `docs/requirements.md` v0.2, `docs/architecture.md` v0.2, `docs/impl-plan.md` v0.2, `src/docs/implementation.md` v0.1 (not yet accepted by a human), source and tests under `src/` |

## 1. Environment

| Item | Value |
|---|---|
| OS | Windows 11 Enterprise 10.0.26200 |
| JDK | Eclipse Temurin 17.0.20.1 (`C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`) |
| Maven | Apache Maven 3.10.0 (`C:\Program Files\Apache\Maven`) |
| Spring Boot | 3.3.5 (from `pom.xml`) |
| Latest run (UTC) | 2026-10-26T13:08:51Z |

## 2. Executed Checks

| # | Command | Exit | Outcome |
|---|---|---|---|
| E1 | `mvn -B verify` (latest run, 2026-10-26 13:08 UTC) | 1 | **BUILD FAILURE.** 29 tests run, 28 pass, **1 failure** (`HabitApiTest.serverBindsToLocalhostOnly`, see D-3). |
| E2 | `java -jar target/routine-stacker-0.1.0.jar` (earlier run, 09:15 UTC) | Failed | **Environment blocker B-1.** Tomcat could not start its connector. |
| E3 | `java -jar target/routine-stacker-0.1.0.jar` (latest run, 13:08 UTC), then `curl` to `127.0.0.1:8080` and `localhost:8080` | Failed | **Environment blocker B-1 again.** Same `Invalid argument: connect` error from `PipeImpl` / `UnixDomainSockets`. Every `curl` returned `000` (no connection). |

**E4 (`mvn -B clean verify`, 2026-10-26, after `server.address` was set to `localhost`):** BUILD SUCCESS, exit 0. 29 tests run, 0 failures, 0 errors, 0 skipped. This replaces E1 as the current automated result. D-3 is resolved by this change.

**E5 (`mvn -B clean verify`, 2026-10-26, latest run, after the concurrency test was strengthened):** BUILD SUCCESS, exit 0. 30 tests run, 0 failures, 0 errors, 0 skipped. This supersedes E4 (29 tests) as the current automated result. Review finding #1 (Major, NFR-002: the mixed undo/check-in test could not detect dropped check-ins) is resolved by the new test `simultaneousCheckInsOnDistinctDaysKeepEveryCheckIn`: 50 threads add 50 distinct dates and assert an exact count of 50.

**E6 (`mvn -B clean verify`, 2026-10-26, latest run, after the code-review fixes):** BUILD SUCCESS, exit 0. 35 tests run, 0 failures, 0 errors, 0 skipped. This supersedes E5 (30 tests) as the current automated result. Code changes since E5:
- `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`, so Spring MVC errors (405, 415, unknown-route 404) use the `ErrorResponse` shape. A catch-all 500 handler returns a generic message with no stack trace. Covered by `HabitApiTest` tests for 405, 415, and 404.
- `--border` in `styles.css` changed from `#9ca3af` to `#6b7280` (4.83:1 on white, 4.39:1 on the page background, calculated). Not measured in a browser.
- `app.js` restores focus to the matching toggle after each check-in or undo (checked with `node --check` only).
- `index.html` and `app.js` show create-form errors in a new `#form-error` line inside the form.
- New ordering tests: `HabitApiTest.listReturnsHabitsInCreationOrder` (REQ-002) and `CheckInServiceTest.historyIsNewestFirstRegardlessOfInsertionOrder` (REQ-005).

Test results by class (from E6, latest run):

| Test class | Tests | Result |
|---|---|---|
| `api.HabitApiTest` | 21 | Pass |
| `api.LatencyTest` | 1 | Pass |
| `repository.InMemoryHabitRepositoryConcurrencyTest` | 4 | Pass |
| `service.CheckInServiceTest` | 1 | Pass |
| `service.StreakCalculatorTest` | 8 | Pass |

Failure detail (E1):

```
HabitApiTest.serverBindsToLocalhostOnly:44 expected: <localhost> but was: <127.0.0.1>
```

The earlier run (09:15 UTC) recorded 29 of 29 passing. The test and the properties file were in the same state then, so the earlier result is not reproduced. The latest run is the current result, and the earlier pass should not be relied on.

## 3. Not Run (Required Checks)

| Check | Reason | Status |
|---|---|---|
| Live HTTP smoke test (create, list, check-in, undo, history, error shape, unknown route) | E2 and E3 blocked: no loopback socket in this environment | **Not Run** |
| Static UI served at `/` | Same as above | **Not Run** |
| TASK-014 browser checks: 360 px and 1920 px layouts, keyboard-only flow, focus visibility, contrast, console errors | Requires a running app | **Not Run** |
| Restart check (NFR-009) | Requires a running app | **Not Run** |

## 4. Acceptance Criteria Verdicts

Verdicts are based on the executed checks in section 2. Where a criterion is covered only by a test class, the test class is named.

| Criterion | Evidence | Verdict |
|---|---|---|
| REQ-001 create (7 scenarios) | `HabitApiTest` create, validation, and duplicate tests; concurrency test | **Pass** (API-level, MockMvc) |
| REQ-002 list (2 scenarios) | `HabitApiTest.listIsEmptyWhenNoHabitsExist`. Creation-order listing is not asserted by an API test. | **Pass** (partial: order not verified by test) |
| REQ-003 check in (3 scenarios) | `HabitApiTest` check-in tests | **Pass** (API-level) |
| REQ-004 undo (2 scenarios) | `HabitApiTest` undo tests | **Pass** (API-level) |
| REQ-005 history (2 scenarios) | `HabitApiTest` covers the unknown-habit 404 only. Newest-first ordering has no test. | **Not Run** for newest-first ordering |
| REQ-006 streak (4 scenarios) | `StreakCalculatorTest` (all scenarios) | **Pass** |
| REQ-007 responsive UI (3 scenarios) | None executed | **Not Run** |
| NFR-001 p95 ≤ 500 ms | `LatencyTest` at 1,000 habits and 100,000 check-ins (service level, in-process) | **Pass** (service layer, not HTTP) |
| NFR-002 concurrency | `InMemoryHabitRepositoryConcurrencyTest` (20 creates, 50 check-ins, 50 mixed operations) | **Pass** |
| NFR-003 validation | `HabitApiTest` validation tests | **Pass** |
| NFR-004 error shape | `HabitApiTest` error assertions. Framework-generated errors (for example, 405) do not use the app's error shape. | **Pass** for application errors; **Fail** for framework errors (see D-1) |
| Localhost-only binding (architecture D-13, OQ-A5; impl-plan TASK-001, TASK-012) | `HabitApiTest.serverBindsToLocalhostOnly` | **Fail** (see D-3) |
| NFR-005 accessibility | None executed | **Not Run** |
| NFR-006 responsiveness | None executed | **Not Run** |
| NFR-007 usability | None executed | **Not Run** |
| NFR-008 technology | Build uses Java 17, Spring Boot, and vanilla static assets | **Pass** |
| NFR-009 durability | In-memory only by design. Restart check not run. | **Not Run** (restart check) |

## 5. Defects

| ID | Type | Description | Severity | Owner |
|---|---|---|---|---|
| D-1 | Product | Framework-generated errors (for example, 405 Method Not Allowed) do not use the NFR-004 `ErrorResponse` shape. `GlobalExceptionHandler` handles only application and body-parsing errors. Carried over from the implementation report, deviation 2. | Medium | Implementation |
| D-2 | Gap | Newest-first history ordering has no test. Carried over from the implementation report, deviation 3. | Low | Implementation |
| D-3 | Product / configuration | `src/main/resources/application.properties` sets `server.address=127.0.0.1`. Architecture D-13 and OQ-A5 require `localhost`, and impl-plan TASK-001 says `server.address=localhost`. `HabitApiTest.serverBindsToLocalhostOnly` fails on this mismatch (E1). **Resolved:** `server.address=localhost` is set; E4 to E6 pass. | Medium | Implementation (resolved) |

No other product defect was found in the executed checks. D-1, D-2, and D-3 are contract and coverage gaps. Route all three to Implementation for a decision. Verification does not fix them. Changing `server.address` to `localhost` would address D-3, but that change belongs to Implementation, and the live check should be repeated after it.

## 6. Environment Blockers

| ID | Blocker | Effect | Needed to clear |
|---|---|---|---|
| B-1 | Java cannot create a selector on this machine. `Selector.open()` fails with `Unable to establish loopback connection` (`PipeImpl`, `Invalid argument: connect`). Tomcat cannot start. | E2 and E3 cannot run. All live HTTP and browser checks are Not Run. | Investigate the host networking or security software that blocks the JVM's loopback pipe, then rerun E2 and E3. |

**Attempt history for B-1:**

| Date (UTC) | Attempt | Result |
|---|---|---|
| 2026-10-26 09:15 | `java -jar target/routine-stacker-0.1.0.jar` outside the sandbox | Failed with the `PipeImpl` error. The sandbox was not the only cause. |
| 2026-10-26 09:15 | Same, with `-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.WindowsSelectorProvider` | Failed with the same error. |
| 2026-10-26 09:15 | Standalone JDK probe (`LoopCheck.java`) | TCP bind on `localhost` succeeded. `Selector.open()` failed with the same error. |
| 2026-10-26 09:15 | `java -Djava.net.preferIPv4Stack=true -jar target/routine-stacker-0.1.0.jar` outside the sandbox | Failed with the same `Invalid argument: connect` error. The requested `0.0.1-SNAPSHOT` jar does not exist; the build produces `0.1.0`. |
| 2026-10-26 13:08 | `java -jar target/routine-stacker-0.1.0.jar` (E3), then `curl` checks | Failed with the same `Invalid argument: connect` error. All `curl` requests returned `000`. |

Conclusion: TCP works, but the JVM's selector setup is blocked. The app cannot serve HTTP on this machine until that is fixed. No code change would help. Live checks stay Not Run.

## 7. Verdict

**Overall: Not Passed (Blocked). Automated checks pass; live checks are waived as blocked (B-1), pending your acceptance.**

- Automated checks: 35 of 35 tests pass on E6 (`mvn clean verify`). D-3 is resolved by setting `server.address=localhost`.
- Code-review findings 1 to 5 were fixed after E5 and are covered by E6. Their browser behaviour (contrast, focus, error placement) has not been checked, under waiver B-1.
- Waiver B-1: the live HTTP and browser checks could not run on this machine. The waiver is recorded here, pending your decision.
- Required checks not run: live HTTP smoke test, REQ-007 browser checks, NFR-005 to NFR-007, and the NFR-009 restart check.
- Per the verification rules, any required check not run prevents an overall pass unless you explicitly approve dropping that criterion. The failing test (D-3) also blocks a pass.

## 8. Next Steps

1. Implementation decides D-3: set `server.address=localhost` in `application.properties` (matches the approved design), then rerun `mvn -B verify`.
2. Implementation decides D-1 and D-2.
3. Clear B-1 (host or security software blocking the JVM loopback selector). Then run the app outside the sandbox with `java -jar target/routine-stacker-0.1.0.jar` or `mvn spring-boot:run`, open `http://localhost:8080`, repeat the API smoke test, and complete the TASK-014 browser checks. Record the results here.

## Approvals

| Field | Value |
|---|---|
| Approved | Yes |
| Approved by | Gurpreet Chawla (user) |
| Approval date | 2026-10-26 |
| Scope approved | Automated verification evidence: E6 `mvn clean verify`, 35 of 35 tests pass. Waiver B-1: live HTTP, browser, and restart checks (NFR-009, REQ-007, NFR-005 to NFR-007) remain Not Run because the JVM cannot open a loopback socket on this machine. |
| Notes | Verdict is Not Passed (Blocked) for the live checks. Approved on the basis of the passing automated run (E6) and the B-1 waiver. The E5 approval (30 of 30) was superseded by E6 after the code-review fixes. Approval recorded through the phase gate (`review`) on 2026-10-26T15:11:01Z. |

> Approvals are recorded only after the user explicitly confirms them. Verification evidence is not approved until the user accepts it.
