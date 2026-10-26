# Routine Stacker

A lightweight Java Spring Boot and web habit-tracking application for building and maintaining daily routines. Each habit is "stacked" onto an existing anchor routine, so a new habit is triggered by something you already do.

> **Status:** This repository currently holds the workspace configuration and SDLC governance only. The Spring Boot application code has not been committed yet. The build and run steps below will be confirmed once the project is scaffolded.

## Architecture and SDLC governance

Work follows a six-phase Agentic SDLC. Each phase needs a human approval before the next one starts.

| # | Phase | Agent | Output |
|---|-------|-------|--------|
| 1 | Requirements | Requirements Agent | `docs/requirements.md` |
| 2 | Architecture | Architecture Agent | `docs/architecture.md` |
| 3 | Implementation Plan | Implementation Planner | `docs/impl-plan.md` |
| 4 | Code | Implementation Agent | `src/` |
| 5 | Verification | Verification Agent | `tests/` |
| 6 | PR and Release | PR and Confluence Agent | GitHub PR and Confluence page |

### Phase state

`.claude/sdlc_state.json` records the active phase, each phase's status, the approval history, and any feedback given to an agent. It is the single source of truth for where the workflow stands.

### Gate hooks

Two hooks in `.claude/settings.json` enforce the sequence:

- **`PreToolUse`** runs `phase_gate.py pre` before any `Write`, `Edit`, or `MultiEdit`. If the file belongs to a phase other than the active one, the write is denied and the reply names the phase that must be approved first.
- **`PostToolUse`** runs `phase_gate.py post` after a write. It marks the active phase as in progress the first time its artifact is written.

A separate hook, `hitl_gate.py`, pauses for explicit approval before higher-risk actions: merging pull requests, pushing to protected branches, running migrations or infrastructure changes, deleting files, and overwriting existing files. It also flags any action aimed at a repository other than `gurpreetchawla26/routine-stacker`.

### Approving a phase

When an agent finishes its artifact, review and advance the workflow from a terminal in the project root:

```bash
python .claude/hooks/scripts/phase_gate.py review
```

The command shows the phase's artifacts and asks `[y]es / [n]o / [f]eedback`:

- **y** approves the phase and moves the active phase to the next one.
- **n** leaves the phase unchanged.
- **f** records your feedback in `.claude/sdlc_state.json`. The agent reads it there and revises its output.

To see the current phase without changing anything:

```bash
python .claude/hooks/scripts/phase_gate.py status
```

## Tooling and MCP integration

Three MCP servers are configured in `.mcp.json`:

| Server | Purpose |
|--------|---------|
| `github` | Pull requests, branches, and files in `gurpreetchawla26/routine-stacker` |
| `jira` | Issues in the CLAUDE project (for example CLAUDE-2) |
| `claude-session-manager` | Session management. Pinned to `mcp<2` because newer releases of the `mcp` library break it. |

Credentials are never stored in the repository. `.mcp.json` refers to environment variables instead: `GITHUB_PERSONAL_ACCESS_TOKEN`, `JIRA_USERNAME`, and `JIRA_API_TOKEN`.

### Context optimization

- **`.claudeignore`** lists runtime cache, logs, build output, and dependencies that should stay out of context. Claude Code does not read this file itself, so it is kept as a shared reference.
- **Read-deny rules** in `.claude/settings.json` are the mechanism that actually keeps those paths out of context: `Read(./.claude/.cache/**)` and `Read(./.claude/hooks/logs/**)`.
- **`.claude/.cache/`** holds temporary runtime data. Run `python .claude/hooks/scripts/clear_cache.py` to empty it.

## Development quickstart

### Prerequisites

- Java 17 or later
- Maven or Gradle (to be confirmed when the project is scaffolded)
- Claude Code CLI, signed in to your account
- Node.js and `npx` (for the GitHub and Jira MCP servers)
- Python 3 (for the gate hooks)
- `uv` (for the `claude-session-manager` MCP server)

### Environment variables

Set these before starting Claude Code so the MCP servers can authenticate:

```bash
export GITHUB_PERSONAL_ACCESS_TOKEN=...
export JIRA_USERNAME=you@example.com
export JIRA_API_TOKEN=...
```

### Run the SDLC workflow

1. Open a terminal in the project root and start Claude Code with `claude`.
2. Check the active phase with `python .claude/hooks/scripts/phase_gate.py status`.
3. Let the agent for that phase produce its artifact.
4. Review and advance with `python .claude/hooks/scripts/phase_gate.py review`.
5. Repeat for the next phase.

### Build and run the application

Build and run steps will be added once the Spring Boot application is committed to this repository.
