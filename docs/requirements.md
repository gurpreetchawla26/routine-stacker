# Routine Stacker: Requirements

## Document Metadata

| Field | Value |
|---|---|
| Status | Ready for Final Review |
| Phase | 1 of 6: Requirements |
| Author | Requirements Agent (AI-drafted for user review) |
| Date | 2026-10-26 |
| Version | 0.2 |
| Repository | gurpreetchawla26/routine-stacker |

## 1. Executive Summary and Personas

Routine Stacker is a lightweight habit-tracking web application for building and maintaining daily routines. Each habit is "stacked" onto an existing anchor routine, so that a new habit is triggered by something the user already does (for example, "After I make coffee, I will stretch for 5 minutes").

**Persona: Individual user (P-1).** A single person using a browser on a desktop or phone to define habits, stack them onto routines they already have, and check them off each day. There is no concept of multiple users or accounts.

## 2. Scope and Boundaries

### 2.1 In Scope

- Create a habit with a name, an anchor routine, and a frequency.
- View all habits and their anchor routines.
- Check in a habit as completed for the current day.
- Undo a check-in for the current day.
- View the check-in history of a habit.
- Show the current streak of consecutive completed periods for a habit.
- A single-page, responsive web UI served by the backend.

### 2.2 Out of Scope

- User accounts, login, or authentication.
- Multi-user, multi-tenant, or sharing features.
- Persistence across restarts. Data lives only in memory and is lost when the application stops.
- An external database, cache, or message broker.
- Push notifications, email, or calendar reminders.
- Editing and deleting habits (OQ-4: excluded from this release).
- Mobile native apps and any frontend framework (React, Vue, Angular, or similar).
- Analytics beyond the streak and history described above.

## 3. Glossary

| Term | Meaning |
|---|---|
| Habit | A repeatable action the user wants to perform, with a name, an anchor routine, and a frequency. |
| Anchor routine | The existing daily action a habit is stacked onto. Stored as free text (OQ-1). |
| Frequency | How often a habit is expected: `DAILY` or `WEEKLY`. |
| Check-in | A record that a habit was completed on a given date. |
| Streak | The number of consecutive expected periods (days or weeks) in which a habit has a check-in. |

## 4. Functional Requirements

All REQ-001 to REQ-007 items are in scope for this release.

### REQ-001: Create a habit (Confirmed in project description)

**User story:** As a user, I want to create a habit and stack it onto an anchor routine, so that I can trigger the new habit from something I already do.

- **Inputs:** `name` (required, 1 to 100 characters after trimming), `anchorRoutine` (required, 1 to 150 characters after trimming), `frequency` (required, `DAILY` or `WEEKLY`).
- **Output:** The created habit with a unique `id`, the three fields above, and `createdAt`.
- **Rules:** `name` must be unique among habits, compared case-insensitively after trimming.

### REQ-002: List habits (Confirmed in project description)

**User story:** As a user, I want to see all my habits and their anchor routines, so that I can review my stack at a glance.

- **Output:** All habits, ordered by creation time (oldest first). An empty list is returned when there are no habits.

### REQ-003: Check in a habit for today (Confirmed)

**User story:** As a user, I want to mark a habit as done for today, so that my progress is recorded.

- **Inputs:** `habitId`. The date is always the server's local date (OQ-2).
- **Rules:** A habit can have at most one check-in per date (OQ-6). A second check-in on the same date is a no-op and is not an error.

### REQ-004: Undo today's check-in (Confirmed)

**User story:** As a user, I want to undo a check-in I made by mistake, so that my history stays accurate.

- **Inputs:** `habitId`. Applies only to the current date.
- **Rules:** Removing a check-in that does not exist is a no-op and is not an error.

### REQ-005: View check-in history (Confirmed)

**User story:** As a user, I want to see the dates on which I completed a habit, so that I can review my consistency.

- **Output:** Check-in dates for the habit, newest first.

### REQ-006: Show current streak (Confirmed)

**User story:** As a user, I want to see how many consecutive periods I have completed a habit, so that I stay motivated to continue.

- **Rules:**
  - For `DAILY` habits, a streak is the number of consecutive calendar days ending today (or yesterday, if today has no check-in yet) that have a check-in.
  - For `WEEKLY` habits, a streak is the number of consecutive calendar weeks ending this week (or last week, if this week has no check-in yet) that have a check-in. Weeks start on Monday (OQ-3).
  - A habit with no qualifying check-ins has a streak of `0`.

### REQ-007: Responsive web interface (Confirmed in project description)

**User story:** As a user, I want to use the application on a phone or a desktop, so that I can check in from wherever I am.

- Every REQ-001 to REQ-006 function is reachable from a single page served by the backend.

## 5. Acceptance Criteria

### REQ-001: Create a habit

```gherkin
Scenario: Successfully create a habit
  Given the habit list is empty
  When a client POSTs {"name": "Stretch", "anchorRoutine": "After coffee", "frequency": "DAILY"} to /api/habits
  Then the response status is 201 Created
  And the body contains a unique "id", the submitted fields, and "createdAt"
  And a subsequent GET /api/habits includes the new habit

Scenario: Reject a blank name
  Given the habit list is empty
  When a client POSTs a habit whose "name" is "   "
  Then the response status is 400 Bad Request
  And the body names the field "name" with a message stating it is required

Scenario: Reject a name longer than 100 characters
  When a client POSTs a habit whose "name" has 101 characters
  Then the response status is 400 Bad Request
  And the body names the field "name" with a length message

Scenario: Reject a blank anchor routine
  When a client POSTs a habit whose "anchorRoutine" is ""
  Then the response status is 400 Bad Request
  And the body names the field "anchorRoutine"

Scenario: Reject an unknown frequency
  When a client POSTs a habit whose "frequency" is "MONTHLY"
  Then the response status is 400 Bad Request
  And the body names the field "frequency" and lists the allowed values "DAILY" and "WEEKLY"

Scenario: Reject a duplicate name regardless of case
  Given a habit named "Stretch" exists
  When a client POSTs a habit named "  stretch  "
  Then the response status is 409 Conflict
  And no second habit is stored

Scenario: Concurrent creation of the same name
  Given no habit named "Read" exists
  When 20 clients simultaneously POST a habit named "Read"
  Then exactly one response is 201 Created
  And the other 19 responses are 409 Conflict
  And GET /api/habits returns exactly one habit named "Read"
```

### REQ-002: List habits

```gherkin
Scenario: List habits in creation order
  Given habits "A" and then "B" were created in that order
  When a client GETs /api/habits
  Then the response status is 200 OK
  And the habits appear in the order "A", then "B"

Scenario: Empty list
  Given no habits exist
  When a client GETs /api/habits
  Then the response status is 200 OK
  And the body is an empty array
```

### REQ-003: Check in a habit for today

```gherkin
Scenario: Check in an existing habit
  Given habit "Stretch" exists with no check-in today
  When a client POSTs /api/habits/{id}/check-ins
  Then the response status is 201 Created
  And the check-in is recorded with today's date

Scenario: Duplicate check-in on the same day
  Given habit "Stretch" already has a check-in today
  When a client POSTs /api/habits/{id}/check-ins again
  Then the response status is 200 OK
  And exactly one check-in for today exists

Scenario: Check in an unknown habit
  When a client POSTs /api/habits/unknown-id/check-ins
  Then the response status is 404 Not Found
```

### REQ-004: Undo today's check-in

```gherkin
Scenario: Undo a check-in made today
  Given habit "Stretch" has a check-in today
  When a client DELETEs /api/habits/{id}/check-ins/today
  Then the response status is 204 No Content
  And the habit has no check-in today

Scenario: Undo when no check-in exists
  Given habit "Stretch" has no check-in today
  When a client DELETEs /api/habits/{id}/check-ins/today
  Then the response status is 204 No Content
```

### REQ-005: View check-in history

```gherkin
Scenario: History is newest first
  Given habit "Stretch" has check-ins on 2026-10-20, 2026-10-22, and 2026-10-23
  When a client GETs /api/habits/{id}/check-ins
  Then the dates are returned in the order 2026-10-23, 2026-10-22, 2026-10-20

Scenario: History of a habit with no check-ins
  Given habit "Stretch" has no check-ins
  When a client GETs /api/habits/{id}/check-ins
  Then the response status is 200 OK
  And the body is an empty array
```

### REQ-006: Show current streak

```gherkin
Scenario: Daily streak including today
  Given habit "Stretch" is DAILY with check-ins today, yesterday, and the day before
  And no check-in exists for the day before that
  When a client GETs /api/habits/{id}
  Then "streak" is 3

Scenario: Daily streak that has not yet been checked in today
  Given habit "Stretch" is DAILY with check-ins yesterday and the day before
  And no check-in exists today
  When a client GETs /api/habits/{id}
  Then "streak" is 2

Scenario: Streak broken by a missed day
  Given habit "Stretch" is DAILY with check-ins two days ago and today, but not yesterday
  When a client GETs /api/habits/{id}
  Then "streak" is 1

Scenario: No check-ins
  Given habit "Stretch" has no check-ins
  When a client GETs /api/habits/{id}
  Then "streak" is 0
```

### REQ-007: Responsive web interface

```gherkin
Scenario: Usable on a narrow phone screen
  Given the page is loaded in a 360 px wide viewport
  When the user views the habit list
  Then no horizontal scrolling is required
  And every control is reachable and operable by touch

Scenario: Usable on a wide desktop screen
  Given the page is loaded in a 1920 px wide viewport
  When the user views the habit list
  Then the content is readable without horizontal scrolling

Scenario: Keyboard-only use
  Given the page is loaded in a desktop browser
  When the user uses only the Tab, Enter, and Space keys
  Then every REQ-001 to REQ-006 action can be completed
  And the focused element has a visible focus indicator
```

## 6. Non-Functional Requirements

| ID | Category | Requirement | Measure |
|---|---|---|---|
| NFR-001 | Performance | Each REST call completes within a sub-second local response. | p95 latency ≤ 500 ms for any endpoint on a single local instance with 1,000 habits and 100,000 check-ins (assumption A-5). |
| NFR-002 | Concurrency | All in-memory collections are thread-safe. Concurrent requests must not corrupt state, create duplicates, or lose check-ins. | Concurrency test with 50 threads across mixed create, check-in, and undo operations leaves state consistent with the successful responses. |
| NFR-003 | Data validation | All invalid payloads are rejected before any state change. | 100% of the validation rules in REQ-001 return 400 with a field-level message. |
| NFR-004 | Error handling | Error responses use one consistent JSON shape and never expose stack traces. | Every 4xx and 5xx response has the shape `{"timestamp", "status", "error", "message", "path"}`. |
| NFR-005 | Accessibility | The UI meets WCAG 2.1 AA. | Text contrast ≥ 4.5:1 (≥ 3:1 for large text). All controls have accessible names. Full keyboard operation. Focus indicator is visible. |
| NFR-006 | Responsiveness | The layout works from 360 px to 1920 px wide. | No horizontal scroll at 360 px. Touch targets are at least 44 × 44 px. |
| NFR-007 | Usability | Every error is shown in plain language next to the field or action that failed. | No raw error codes or JSON shown to the user. |
| NFR-008 | Technology | The backend is Java with Spring Boot. The frontend is semantic HTML, vanilla JavaScript, and CSS, served from `src/main/resources/static/`. | Verified in the architecture and implementation reviews. |
| NFR-009 | Durability | Data is held only in memory. Restarting the application clears all habits and check-ins. | Stated on the UI so users are not misled. Verified by a restart test. |

## 7. Fixed Constraints

- Backend: Java with Spring Boot REST APIs.
- Persistence: thread-safe in-memory storage, normally `ConcurrentHashMap`. No external database.
- Frontend: zero-build semantic HTML, vanilla JavaScript, and CSS. No heavy frontend framework.
- No authentication, accounts, or external services.

## 8. Assumptions

| ID | Assumption | Status |
|---|---|---|
| A-1 | Single user per running instance, with no login. | Pending final review |
| A-2 | "Today" is the server's local calendar date. | Confirmed (OQ-2) |
| A-3 | Anchor routine is free text, not a reference to a separate routine entity. | Confirmed (OQ-1) |
| A-4 | Frequency options are limited to `DAILY` and `WEEKLY`. | Pending final review |
| A-5 | The expected load is small (up to 1,000 habits) and a single local instance is sufficient. | Pending final review |
| A-6 | A habit has no target count per period. One check-in per period counts as complete. | Confirmed (OQ-6) |

## 9. Dependencies

- Java 17 or later and Maven or Gradle (to be confirmed in the architecture phase).
- A modern desktop and mobile browser for the web UI.

## 10. Risks

| ID | Risk | Impact | Mitigation |
|---|---|---|---|
| R-1 | All data is lost on restart, which users may not expect. | Lost progress and user frustration. | State clearly in the UI and README (NFR-009). Revisit persistence in a later phase if the user asks. |
| R-2 | Streak rules differ between users' expectations, especially for weekly habits and time zones. | Confusing streak values. | Rules fixed by OQ-2 and OQ-3. Cover them with explicit tests. |
| R-3 | Concurrent check-ins or creates could corrupt in-memory state. | Duplicate or lost data. | NFR-002 concurrency tests. Use `ConcurrentHashMap` with atomic operations. |

## 11. Open Questions (Resolved)

| ID | Question | Decision |
|---|---|---|
| OQ-1 | Should an anchor routine be free text on each habit, or a separate entity shared by many habits? | Free text. |
| OQ-2 | Which time zone defines "today"? | The server's local date. No time-zone setting. |
| OQ-3 | For `WEEKLY` habits, does a week start on Monday or Sunday? | Monday. |
| OQ-4 | Are editing and deleting habits needed in this release? | Excluded from this release. |
| OQ-5 | Should REQ-003 to REQ-006 (check-in, undo, history, streak) be in this release? | Yes, all included. |
| OQ-6 | Can a habit have more than one check-in per day? | No. A habit can be checked in at most once per day. |

## 12. Out-of-Scope Items

Listed in section 2.2. Any of these added later requires a new requirements change and makes dependent phase approvals stale.

## 13. Traceability Matrix

| Requirement | Status | Acceptance criteria | Verified by (later phase) |
|---|---|---|---|
| REQ-001 Create a habit | Confirmed | 7 scenarios in section 5 | Unit and API tests |
| REQ-002 List habits | Confirmed | 2 scenarios | API tests |
| REQ-003 Check in for today | Confirmed | 3 scenarios | API tests |
| REQ-004 Undo today's check-in | Confirmed | 2 scenarios | API tests |
| REQ-005 View history | Confirmed | 2 scenarios | API tests |
| REQ-006 Show streak | Confirmed | 4 scenarios | Unit tests for streak logic and API tests |
| REQ-007 Responsive interface | Confirmed | 3 scenarios | Browser tests at 360 px, 1920 px, and keyboard-only |
| NFR-001 to NFR-009 | Pending final review | Measures in section 6 | Performance, concurrency, and accessibility checks |

## 14. Decision Log

| Date | Decision | Source |
|---|---|---|
| 2026-10-26 | Persistence is in-memory only. Data resets on restart. | Project description and fixed constraints |
| 2026-10-26 | Habits are stacked onto an anchor routine. | Project description |
| 2026-10-26 | Anchor routine is free text (OQ-1). | User decision |
| 2026-10-26 | "Today" is the server's local date (OQ-2). | User decision |
| 2026-10-26 | Weeks start on Monday (OQ-3). | User decision |
| 2026-10-26 | Edit and delete are excluded from this release (OQ-4). | User decision |
| 2026-10-26 | REQ-003 to REQ-006 (check-in, undo, history, streak) are included (OQ-5). | User decision |
| 2026-10-26 | A habit can be checked in at most once per day (OQ-6). | User decision |
| 2026-10-26 | Requirements phase re-opened after approval to record the decisions above (v0.2). Prior approval is stale. | User request |

## Approvals

| Field | Value |
|---|---|
| Approved | No |
| Approved by | — |
| Approval date | — |
| Scope approved | — |
| Notes | v0.2 reflects the user's finalized decisions and is ready for final review. Approval is recorded through `phase_gate.py review` after the user explicitly confirms it. The earlier approval (2026-10-26T08:15:58Z) applied to v0.1 and is stale. |

> Approvals are recorded only after the user explicitly confirms them. Any change to this document after approval makes the architecture, implementation plan, and all later phase approvals stale.
