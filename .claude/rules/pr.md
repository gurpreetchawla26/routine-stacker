---
paths:
  - "src/docs/pr.md"
---
# Phase 8: Pull Request Preparation and Publication

## Ownership
Prerequisites: explicitly approved and current upstream artifacts, passing verification, and approved code review with no unresolved blockers. Own and edit only `src/docs/pr.md`. Git or remote PR actions are permitted only after separate, explicit user authorization for each intended action. Do not edit application code, tests, other phase documents, workflow configuration, or Jira issues.

## Project Constraints
- Describe only the approved Java/Spring Boot, thread-safe in-memory repository, and zero-build static vanilla HTML/JavaScript/CSS solution.
- Do not introduce or advertise an external database, heavy frontend framework, unapproved feature, dependency, or scope change.
- Do not discard user changes, include unrelated files, expose credentials, or put secrets in PR content.
- Phase approval is not permission to create branches, commit, push, or publish a PR.

## Workflow
1. Confirm all prerequisites and approvals are current and that no review blockers remain. Stop if a gate is incomplete.
2. Inspect repository status and proposed diff read-only. Preserve existing changes. Report missing Git, remote, or tooling; do not initialize or connect without approval.
3. Draft a PR title/body covering purpose, scope, requirement IDs, design decisions, actual test results, screenshots if available, risks, restart data-loss limitation, and reviewer checklist.
4. Write `src/docs/pr.md` with `Status: Draft`, prerequisite references, title, body, proposed branches, publication state, and `## Approvals`. State `Not Created` until a real PR exists.
5. Present the exact remote, source/base branches, file/commit scope, and PR content. Request explicit authorization for each branch, commit, push, and PR creation action; STOP and wait.
6. After authorization only, use available tooling. Stage only authorized files, exclude secrets/unrelated changes, and never force-push or route credentials through chat.
7. On success, update only `src/docs/pr.md` with the real URL, branches, commit ID, date, authorization, and outcome. On failure or unavailable tooling, accurately leave the PR as not created and report the blocker.
8. Report completion or blocker. Never invent approval, URL, commit, or test result. Do not claim the SDLC is complete if publication is blocked.
