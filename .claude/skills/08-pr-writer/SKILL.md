---
name: 08-pr-writer
description: "Prepare and synthesize the final Pull Request documentation, summary, release notes, and reviewer checklist for Routine Stacker in src/docs/pr.md."
user-invocable: true
---

# Pull Request Writer Skill (Phase 8)

Use this skill to compile the complete Pull Request specification, summarize changes across all previous phases, and prepare the pull request artifact for **Routine Stacker**.

## Prerequisites
- Approved `src/docs/requirements.md`
- Approved `src/docs/architecture.md`
- Approved `src/docs/design-review.md`
- Approved `src/docs/implementation-plan.md`
- Approved `src/docs/implementation.md`
- Approved `src/docs/verification.md`
- Approved `src/docs/code-review.md` (with `Pass` disposition)

## Output Target
- Deliverable: `src/docs/pr.md`
- Initial State: `Status: Draft`
- Publication State: `Status: Published` (only AFTER explicit user confirmation to open the PR)

## Scope of Skill
- Synthesizing change summaries, motivation, and user impact.
- Mapping implemented features to requirements (`REQ-xxx`).
- Summarizing verification evidence, test results, and browser behavior.
- Documenting architectural highlights and in-memory persistence characteristics.
- Authoring a comprehensive PR body ready for Git/GitHub pull requests.
- Halting for explicit user authorization before any remote Git action or publication.

## Procedure

1. **Synthesize SDLC Artifacts**:
   - Aggregate key decisions, requirement IDs fulfilled, architecture choices, and code review outcomes.
   - Summarize verification evidence: total tests passed, coverage highlights, manual checks.

2. **Compose Comprehensive PR Description**:
   - Include standard engineering PR sections:
     - **Title**: Clear, conventional title (e.g., `feat(routine-stacker): complete habit-stacking app with Spring Boot backend and static frontend`).
     - **Summary / Overview**: High-level explanation of the application.
     - **Key Features Implemented**: Bullet points linked to `REQ-xxx`.
     - **Architecture & Tech Stack**: Spring Boot, in-memory `ConcurrentHashMap`, vanilla JS/CSS.
     - **Verification & Test Evidence**: Test execution summary, commands, pass rate.
     - **In-Memory Limitations**: Explicit callout that data resets on restart.
     - **Reviewer Checklist**: Clear checklist for code reviewers.

3. **Generate `src/docs/pr.md`**:
   - Write the markdown document with:
     - Document Status (`Status: Draft`)
     - PR Metadata (Proposed title, target branch, source branch)
     - Full PR Body & Description
     - Change Set & File Modification Summary
     - Traceability Summary (REQ -> TASK -> Code -> Tests)
     - Publication Status (`Not Created` / `Pending User Authorization`)
     - Approvals & Sign-off Section

4. **Human Authorization Gate**:
   - Present the PR draft and summary to the user.
   - Explicitly request confirmation: ask if the user authorizes committing, pushing, or opening the PR on GitHub.
   - Do NOT run `git commit`, `git push`, or `gh pr create` without separate, explicit user approval.
   - Once authorized and executed (if Git remote is configured), update `src/docs/pr.md` to reflect `Status: Published` with PR URL and commit SHA.
