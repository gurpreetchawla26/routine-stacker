"""Phase gate for the routine-stacker SDLC workflow.

Hook use (registered in .claude/settings.json):
    phase_gate.py pre   PreToolUse: deny writes to an artifact outside the active phase.
    phase_gate.py post  PostToolUse: mark the active phase as in progress after its write.

Approval is a deliberate step run in a terminal, because a hook cannot wait for a
typed answer:
    python .claude/hooks/scripts/phase_gate.py review
It shows the active phase's artifacts, then asks [y]es / [n]o / [f]eedback.
  y  approves the phase and moves the active phase to the next one.
  n  leaves the phase active, unchanged.
  f  stores feedback in the state file. The agent reads it from there and revises.

Set CLAUDE_PHASE_STATE to point at another state file (used for testing).
"""

import argparse
import json
import os
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
DEFAULT_STATE = Path(os.environ.get("CLAUDE_PHASE_STATE") or ROOT / ".claude" / "sdlc_state.json")

# Write targets (relative to the workspace root, POSIX style) mapped to phase ids.
ARTIFACT_RULES = [
    ("requirements", re.compile(r"^docs/requirements\.md$")),
    ("architecture", re.compile(r"^docs/architecture\.md$")),
    ("implementation-planner", re.compile(r"^docs/impl-plan\.md$")),
    ("implementation", re.compile(r"^src/.+")),
    ("verification", re.compile(r"^tests/.+")),
]
WRITE_TOOLS = {"Write", "Edit", "MultiEdit"}


def load_state(path):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def save_state(state, path):
    # Plain utf-8 without a byte-order mark.
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(state, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def now():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def relative_target(raw_path):
    """Return the write target relative to ROOT, or None if it lies outside the workspace."""
    target = Path(raw_path)
    if not target.is_absolute():
        target = ROOT / target
    try:
        return target.resolve().relative_to(ROOT).as_posix()
    except ValueError:
        return None


def phase_for(relpath):
    for phase_id, pattern in ARTIFACT_RULES:
        if pattern.search(relpath):
            return phase_id
    return None


def phase_index(state, phase_id):
    for index, phase in enumerate(state["phases"]):
        if phase["id"] == phase_id:
            return index
    raise KeyError(phase_id)


def active_phase(state):
    return state["phases"][phase_index(state, state["active_phase"])]


def run_pre(payload):
    if str(payload.get("tool_name", "")) not in WRITE_TOOLS:
        return {"continue": True}
    raw_path = str((payload.get("tool_input") or {}).get("file_path", ""))
    relpath = relative_target(raw_path) if raw_path else None
    phase_id = phase_for(relpath) if relpath else None
    if phase_id is None:
        return {"continue": True}

    try:
        state = load_state(DEFAULT_STATE)
    except (OSError, ValueError) as error:
        return deny(f"Phase gate could not read {DEFAULT_STATE}: {error}. Writes to "
                    f"{relpath} are blocked until the state file is fixed.")

    current = active_phase(state)
    if phase_id != current["id"]:
        owner = state["phases"][phase_index(state, phase_id)]["agent"]
        return deny(f"{relpath} belongs to {owner} (phase '{phase_id}'), but the active phase "
                    f"is '{current['id']}' ({current['agent']}). Approve the active phase first "
                    f"with: python .claude/hooks/scripts/phase_gate.py review")
    return {"continue": True}


def run_post(payload):
    if str(payload.get("tool_name", "")) not in WRITE_TOOLS:
        return {"continue": True}
    raw_path = str((payload.get("tool_input") or {}).get("file_path", ""))
    relpath = relative_target(raw_path) if raw_path else None
    phase_id = phase_for(relpath) if relpath else None
    if phase_id is None:
        return {"continue": True}
    try:
        state = load_state(DEFAULT_STATE)
    except (OSError, ValueError):
        return {"continue": True}
    if phase_id == state["active_phase"]:
        phase = active_phase(state)
        if phase["status"] == "pending":
            phase["status"] = "in_progress"
            save_state(state, DEFAULT_STATE)
    return {"continue": True}


def deny(reason):
    return {
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": reason,
        }
    }


def summarize(state, phase):
    lines = [f"Phase: {phase['id']} ({phase['agent']})", f"Status: {phase['status']}", ""]
    for artifact in phase["artifacts"]:
        path = ROOT / artifact
        if path.is_file():
            text = path.read_text(encoding="utf-8", errors="replace")
            headings = [line.strip() for line in text.splitlines() if line.startswith("#")]
            lines.append(f"Artifact: {artifact} ({len(text)} chars)")
            lines += [f"  {heading}" for heading in headings[:15]]
        elif path.is_dir():
            files = [p for p in path.rglob("*") if p.is_file()]
            lines.append(f"Folder: {artifact} ({len(files)} files)")
        else:
            lines.append(f"Missing: {artifact}")
    prior = [item for item in state["feedback"] if item["phase"] == phase["id"]]
    if prior:
        lines += ["", "Earlier feedback:"] + [f"  - {item['text']}" for item in prior]
    return "\n".join(lines)


def run_review(state_path, dry_run):
    state = load_state(state_path)
    index = phase_index(state, state["active_phase"])
    phase = state["phases"][index]
    print(summarize(state, phase))
    nxt = state["phases"][index + 1] if index + 1 < len(state["phases"]) else None
    target = f"{nxt['agent']}" if nxt else "end of workflow"

    while True:
        answer = input(f"\nApprove phase transition to {target}? [y]es / [n]o / [f]eedback: ")
        answer = answer.strip().lower()
        if answer in {"y", "yes"}:
            phase["status"] = "approved"
            state["history"].append({"phase": phase["id"], "decision": "approved", "at": now()})
            if nxt:
                state["active_phase"] = nxt["id"]
                nxt["status"] = "pending"
            print(f"Approved. Active phase is now: {state['active_phase']}")
            break
        if answer in {"n", "no"}:
            state["history"].append({"phase": phase["id"], "decision": "rejected", "at": now()})
            print("Not approved. The phase stays active and nothing advances.")
            break
        if answer in {"f", "feedback"}:
            text = input(f"Feedback for {phase['agent']}: ").strip()
            if not text:
                print("Feedback was empty; nothing recorded.")
                continue
            state["feedback"].append({"phase": phase["id"], "text": text, "at": now()})
            phase["status"] = "in_progress"
            print("Feedback recorded. The agent should read .claude/sdlc_state.json and revise.")
            break
        print("Enter y, n, or f.")

    if dry_run:
        print("Dry run: state file not changed.")
    else:
        save_state(state, state_path)
    return 0


def main(argv):
    if len(argv) >= 2 and argv[1] in {"pre", "post"}:
        try:
            payload = json.load(sys.stdin)
            if not isinstance(payload, dict):
                raise ValueError("hook input must be a JSON object")
        except (ValueError, OSError) as error:
            print(json.dumps(deny(f"Phase gate could not read the tool input: {error}")))
            return 0
        result = run_pre(payload) if argv[1] == "pre" else run_post(payload)
        print(json.dumps(result))
        return 0

    parser = argparse.ArgumentParser(description="Review and advance the SDLC phase")
    sub = parser.add_subparsers(dest="command", required=True)
    review = sub.add_parser("review", help="show the active phase and ask for a decision")
    review.add_argument("--state", type=Path, default=DEFAULT_STATE)
    review.add_argument("--dry-run", action="store_true", help="do not save the decision")
    status = sub.add_parser("status", help="print the active phase")
    status.add_argument("--state", type=Path, default=DEFAULT_STATE)
    args = parser.parse_args(argv[1:])

    if args.command == "status":
        state = load_state(args.state)
        print(summarize(state, active_phase(state)))
        return 0
    return run_review(args.state, args.dry_run)


if __name__ == "__main__":
    sys.exit(main(sys.argv))
