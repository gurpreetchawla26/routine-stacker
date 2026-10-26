"""Append a line to the session history log when a Claude Code session ends.

SessionEnd hook. Records the UTC timestamp, session id, end reason, the active SDLC
phase, and the local git branch, so the log shows how each session finished.
The log lives in .claude/hooks/logs/, which the read-deny rules keep out of context.
Set CLAUDE_SESSION_LOG to write somewhere else (used for testing).
Always exits 0 so a logging problem never affects the session.
"""

import json
import os
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
DEFAULT_LOG = ROOT / ".claude" / "hooks" / "logs" / "session_history.log"
LOG_FILE = Path(os.environ.get("CLAUDE_SESSION_LOG") or DEFAULT_LOG)
STATE_FILE = ROOT / ".claude" / "sdlc_state.json"


def git_branch():
    try:
        result = subprocess.run(["git", "rev-parse", "--abbrev-ref", "HEAD"], cwd=ROOT,
                                capture_output=True, text=True, timeout=5)
        return result.stdout.strip() if result.returncode == 0 else "not-a-repo"
    except (OSError, subprocess.SubprocessError):
        return "unknown"


def active_phase():
    try:
        return json.loads(STATE_FILE.read_text(encoding="utf-8"))["active_phase"]
    except (OSError, ValueError, KeyError):
        return "unknown"


def main():
    try:
        payload = json.load(sys.stdin)
        if not isinstance(payload, dict):
            payload = {}
    except (ValueError, OSError):
        payload = {}

    fields = {
        "time": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "session": str(payload.get("session_id", "unknown")),
        "reason": str(payload.get("reason", "unknown")),
        "phase": active_phase(),
        "branch": git_branch(),
    }
    line = " ".join(f"{key}={value}" for key, value in fields.items())

    try:
        LOG_FILE.parent.mkdir(parents=True, exist_ok=True)
        with open(LOG_FILE, "a", encoding="utf-8", newline="\n") as handle:
            handle.write(line + "\n")
    except OSError:
        pass  # Logging must never break the session.
    return 0


if __name__ == "__main__":
    sys.exit(main())
