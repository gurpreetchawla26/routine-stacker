# Routine Stacker: Code Review

## Document Metadata

| Field | Value |
|---|---|
| Status | Draft |
| Phase | 7 of 8: Code Review (state file phase `code-review`) |
| Date | 2026-10-26 |
| Version | 0.2 |
| Inputs | `docs/requirements.md`, `docs/architecture.md`, `docs/impl-plan.md`, `src/docs/implementation.md` (section 9), `tests/verification.md` (E6), source and tests under `src/` |

## Prerequisite Status

- **Verification:** approved in `.claude/sdlc_state.json` (2026-10-26T15:11:01Z) and in `tests/verification.md`, on waiver B-1. The verdict is "Not Passed (Blocked)" for the live HTTP, browser, and restart checks, which were never run. Automated checks pass: 35 of 35 (E6).
- **Implementation:** status `in_progress`. Its last approval (2026-10-26T09:07:04Z) predates the `localhost` change, the concurrency test, and the review fixes. It has not been re-accepted.

Because verification is not a clean pass, a disposition of Pass cannot be issued.

## Reviewed Scope

`src/main/java/**`, `src/main/resources/**` (`application.properties`, `static/index.html`, `static/app.js`, `static/styles.css`), `src/test/java/**`. Read-only. The 35-test count is a tally of `@Test` methods and matches E6; no build was re-run for this review.

## Prior Blocking Findings (from the first review)

| Prior # | Finding | Status |
|---|---|---|
| 1 | Framework errors (405, 415, 500) outside the NFR-004 shape | **Fixed.** `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`; tests cover 405, 415, and unknown-route 404. Caveat: N3. |
| 2 | `--border` contrast below 3:1 | **Fixed** in code. `#6b7280` calculates to 4.83:1 on white. Not measured in a browser (B-1). |
| 3 | Focus lost after check-in or undo | **Fixed on the success path.** `focusToggle` restores focus. Reload-failure gap: N1. |
| 4 | Create-form errors away from the form | **Fixed.** `#form-error` is inside the form, with `role="alert"`. |
| 5 | No tests for REQ-002 and REQ-005 ordering | **Fixed.** `HabitApiTest.listReturnsHabitsInCreationOrder`, `CheckInServiceTest.historyIsNewestFirstRegardlessOfInsertionOrder`. |

No regressions were found from these fixes. `hidden` works because `styles.css` uses `!important` on it, and `app.js` uses `textContent` only.

## New and Open Findings

Ranked by severity. Owner is Implementation unless noted.

| # | Severity | Blocking | Location | Finding | Remediation |
|---|---|---|---|---|---|
| 6 | Medium | No (needs decision) | `CheckInController.java:28, 35`; architecture line 257 | No CSRF, Origin, Host, or CORS handling on state-changing endpoints. A DNS-rebinding page can reach `localhost:8080`, read `GET /api/habits`, and create habits. A cross-site form POST to check-ins needs the habit UUID, so that path is low risk. | Architecture decision first, then Implementation. |
| N1 | Low | No | `app.js:55-57` (`loadHabits`), `app.js:112-115` (`runAction`) | `loadHabits` catches its own errors and does not rethrow. `runAction`'s catch therefore never runs after a successful action whose reload fails. The clicked button stays disabled, `focusToggle` cannot find an enabled button to focus, and the user cannot retry until a later reload. | Rethrow from `loadHabits`, or re-enable the button in `runAction` when the reload fails. |
| N2 | Low | No | `app.js:32-34`, `app.js:105` | `errorBox` is cleared only in `runAction`. A failed initial load followed by a successful create leaves the stale error visible. | Clear `errorBox` on a successful load. |
| N3 | Low | No | `GlobalExceptionHandler.java:42-45` | The catch-all 500 handler does not log, so server-side failures are invisible. No test covers it. | Log the exception server-side (not to the response), and add a test. |
| N4 | Low | No | `index.html:43`, `app.js:46-48` | No `aria-busy="true"` loading state, which architecture line 225 requires. Not recorded as a deviation. | Add `aria-busy`, or record the deviation. |
| N5 | Info | No | `GlobalExceptionHandler.java:31` | Framework messages pass through `ex.getMessage()`, e.g. "No static resource api/no-such-route." This echoes only the request path. Acceptable. | Document it. |
| 7 | Minor | No | `LatencyTest.java:53-56`; `HabitService.java:54-56` | The latency test times only `service.get`. The list path, which computes streaks for every habit, is not timed (NFR-001). | Time the list path at the NFR-001 load. |
| 8 | Minor | No | `HabitApiTest.java:43-45` | `serverBindsToLocalhostOnly` reads the configured property, not the actual bound address. | Assert against the actual bound address, or document the limitation. |
| 9 | Minor | No | Architecture line 73; `HabitController.java:27` | Architecture says controllers apply `@Valid`. None do; validation lives in `HabitService`. Undocumented deviation. | Record the deviation, or change the architecture. |
| 10 | Minor | No | Test suite | No automated test for `GET /`, although REQ-007 requires the UI to be served by the backend. | Add a MockMvc test for `GET /`. |
| 11 | Minor | No | `InMemoryHabitRepositoryConcurrencyTest.java:50, 79, 108, 139` | `pool.shutdown()` is not in a `finally` block. A failing assertion leaks threads. | Move shutdown into `finally`. |
| 12 | Documentation | No | `tests/verification.md` (REQ-002, REQ-005, D-2 rows) | Still says REQ-002 is partial, REQ-005 is Not Run, and D-2 is open. `HabitApiTest` and `CheckInServiceTest` now cover both. | Verification to re-run and update the traceability table. |

## Test Gaps

- No live HTTP, browser, or restart evidence (waiver B-1).
- REQ-007 and NFR-005 to NFR-007 are not executed.
- No test for the catch-all 500 handler (N3).
- No test for `GET /` (finding 10).

## Residual Risks

- All browser-facing claims (contrast, focus, error placement, and N1 to N4 behaviour) are unverified under waiver B-1.
- The weekly streak rule: `implementation.md` section 9 asks for confirmation of OQ-3, but `requirements.md` marks it decided (Monday). Documentation inconsistency to correct.

## Disposition

**Pass with noted risks.** Decided by Gurpreet Chawla (user), 2026-10-26, after the "Blocked" disposition was rejected.

No blocking code defect remains from the first review. The user accepted the following as documented risks, not as fixes:
- **N1:** a failed list reload after a successful check-in or undo leaves the button disabled until the next reload.
- **N2:** a stale error can stay visible after a failed load followed by a successful create.
- **N3:** the catch-all 500 handler does not log server-side failures, and has no test.
- **N4:** no `aria-busy` loading state, which architecture line 225 requires and which is not recorded as a deviation.
- **Finding 6:** no CSRF, Origin, Host, or CORS checks on state-changing endpoints. A DNS-rebinding page can read `GET /api/habits` and create habits.

Still open, not accepted as risks by this disposition and carried into the PR documentation as known limitations: finding 7 (list path not timed), finding 8 (bound-address test), finding 9 (`@Valid` deviation), finding 10 (no `GET /` test), finding 11 (concurrency pools not shut down in `finally`), and finding 12 (verification doc drift).

Waiver B-1 for the live HTTP, browser, and restart checks is recorded in `tests/verification.md` as accepted on E6. The browser-facing behaviour (contrast, focus, error placement, N1 to N4) remains unverified.

## Approvals

| Field | Value |
|---|---|
| Approved | Yes (disposition: Pass with noted risks) |
| Approved by | Gurpreet Chawla (user) |
| Approval date | 2026-10-26 |
| Scope approved | Pass with noted risks. Accepted documented risks: N1 to N4 and finding 6. Release readiness is approved for the PR phase only. Git, branch, push, and PR actions still need separate authorization. |
| Notes | "Blocked" was rejected by the user. The "Changes Required" disposition of 2026-10-26 is superseded by this review. Approval is recorded in this document; the phase gate (`phase_gate.py review`) still needs to be run by the user to advance the workflow. |
