"""Show a short workspace header when a Claude Code session starts.

SessionStart hook. Prints the active SDLC phase, its agent and expected deliverable,
the target repository, and the local git branch state. Warnings go to the user through
systemMessage; the same text is also given to Claude as additional context.
Always exits 0 so a problem here never blocks a session.
"""

import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
STATE_FILE = ROOT / ".claude" / "sdlc_state.json"
REPOSITORY = "gurpreetchawla26/routine-stacker"
PROTECTED_BRANCHES = {"main", "master"}


def git(args):
    """Return (ok, output) for a git command run in the workspace root."""
    try:
        result = subprocess.run(["git", *args], cwd=ROOT, capture_output=True,
                                text=True, timeout=5)
        return result.returncode == 0, result.stdout.strip()
    except (OSError, subprocess.SubprocessError):
        return False, ""


def phase_lines():
    try:
        state = json.loads(STATE_FILE.read_text(encoding="utf-8"))
        active = state["active_phase"]
        phase = next(p for p in state["phases"] if p["id"] == active)
    except (OSError, ValueError, KeyError, StopIteration) as error:
        return [f"SDLC state unavailable: {error}"], []
    deliverables = ", ".join(phase["artifacts"]) or "none"
    lines = [f"Active SDLC phase: {phase['id']} ({phase['status']})",
             f"Agent role:        {phase['agent']}",
             f"Deliverable:       {deliverables}"]
    warnings = []
    feedback = [item for item in state.get("feedback", []) if item["phase"] == active]
    if feedback:
        warnings.append(f"{len(feedback)} feedback item(s) await revision in sdlc_state.json.")
    return lines, warnings


def branch_lines():
    ok, branch = git(["rev-parse", "--abbrev-ref", "HEAD"])
    if not ok:
        return ["Git:               not a git repository"], [
            "Workspace is not a git repository, so branch checks are skipped."]
    lines = [f"Git branch:        {branch}"]
    warnings = []
    if branch in PROTECTED_BRANCHES:
        warnings.append(f"On protected branch '{branch}'. Create a feature branch before editing.")
    ok, changes = git(["status", "--porcelain"])
    if ok and changes:
        count = len(changes.splitlines())
        lines.append(f"Uncommitted:       {count} changed path(s)")
        warnings.append(f"{count} uncommitted change(s) on branch '{branch}'.")
    return lines, warnings


def main():
    try:
        json.load(sys.stdin)
    except (ValueError, OSError):
        pass  # The payload is not needed for the header.

    phase, phase_warnings = phase_lines()
    branch, branch_warnings = branch_lines()
    header = ["Routine Stacker session", f"Repository:        {REPOSITORY}"]
    header += phase + branch
    warnings = phase_warnings + branch_warnings
    text = "\n".join(header)

    output = {"hookSpecificOutput": {"hookEventName": "SessionStart", "additionalContext": text}}
    if warnings:
        output["systemMessage"] = text + "\n\nWarnings:\n" + "\n".join(f"- {w}" for w in warnings)
    else:
        output["systemMessage"] = text
    print(json.dumps(output))
    return 0


if __name__ == "__main__":
    sys.exit(main())
