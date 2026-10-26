"""Human-in-the-loop approval gate for high-risk or irreversible tool calls.

PreToolUse hook. When a call matches a gated action, the hook returns
permissionDecision "ask", so Claude Code pauses and shows the reviewer a summary
with an Approve / Reject prompt. Rejecting blocks the action, and the reviewer
can say why in the prompt. Calls that match no gate pass through untouched.

If the hook cannot classify a call, it asks rather than allows (fail closed).
"""

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
MAX_DETAIL = 1500

SHELL_TOOLS = {"Bash", "PowerShell"}

# (category, compiled pattern, description, review checklist)
SHELL_GATES = [
    ("merge", re.compile(r"\bgh\s+pr\s+merge\b"),
     "Merging a pull request into the main repository.",
     ["Target PR is the one intended", "Required reviews and CI checks are green",
      "Merge method (squash/merge/rebase) is correct"]),
    ("push", re.compile(r"\bgit\s+push\b"),
     "Pushing commits to a remote repository.",
     ["Branch is correct (not main unless intended)", "Diff and commit messages reviewed",
      "No secrets or generated artifacts included"]),
    ("infrastructure", re.compile(r"\b(terraform\s+(apply|destroy)|pulumi\s+(up|destroy)|"
                                  r"kubectl\s+(apply|delete)|helm\s+(upgrade|uninstall))\b"),
     "Changing live infrastructure.",
     ["Plan or dry-run output reviewed", "Target environment is correct",
      "Rollback path is known"]),
    ("migration", re.compile(r"\b(alembic\s+(upgrade|downgrade)|flyway\s+(migrate|clean)|"
                             r"prisma\s+migrate|manage\.py\s+migrate|db:migrate|"
                             r"dotnet\s+ef\s+database\s+update|liquibase\s+(update|rollback)|"
                             r"knex\s+migrate)\b"),
     "Running a database migration.",
     ["Migration script reviewed", "Target database is correct (not production unless intended)",
      "Backup taken or migration is reversible"]),
    ("destructive", re.compile(r"(\brm\s+-[a-zA-Z]*[rf]|\bRemove-Item\b.*-Recurse|\bdel\s+/[sq]|"
                               r"\brmdir\s+/s|\bgit\s+reset\s+--hard\b|\bgit\s+clean\b.*-[a-zA-Z]*f|"
                               r"\bgit\s+checkout\s+--\s|\b(DROP|TRUNCATE)\s+(TABLE|DATABASE)\b)",
                               re.IGNORECASE),
     "Deleting files or data, or discarding changes.",
     ["Every listed path is expected to be removed", "Nothing uncommitted is lost",
      "Backup exists if data is involved"]),
]

# MCP tools are matched by name. Each entry: (category, pattern, description, checklist)
MCP_GATES = [
    ("merge", re.compile(r"^mcp__github__merge_pull_request$"),
     "Merging a pull request on GitHub.",
     ["Target PR is the one intended", "Reviews and CI checks are green"]),
    ("push", re.compile(r"^mcp__github__(push_files|create_or_update_file)$"),
     "Writing files to a GitHub repository.",
     ["Branch is correct (not main unless intended)", "File contents reviewed"]),
    ("destructive", re.compile(r"^mcp__jira__jira_delete$"),
     "Deleting a Jira resource.",
     ["Item to delete is correct", "Deletion cannot be undone from here"]),
    ("publish", re.compile(r"^mcp__.*confluence.*(create|update|publish|delete|put|post|patch).*$", re.I),
     "Publishing or changing a Confluence page.",
     ["Page title and space are correct", "Content reviewed as final"]),
    ("migration", re.compile(r"^mcp__.*migrat.*$", re.I),
     "Running a database migration through a tool.",
     ["Migration reviewed", "Target database is correct"]),
]

WRITE_TOOLS = {"Write"}

# Calls that target any other repository, or a protected branch, always ask.
ALLOWED_REPO = "gurpreetchawla26/routine-stacker"
PROTECTED_BRANCHES = {"main", "master"}
GH_REPO_SUBCOMMANDS = re.compile(r"\bgh\s+(pr|issue|release|repo|workflow|run|api)\b")
GH_REPO_FLAG = re.compile(r"(?:--repo[= ]|-R\s+)(\S+)")


def push_targets(command):
    """Return (remote, branches) for a git push command. Branches empty means current branch."""
    args = re.search(r"\bgit\s+push\b(.*)", command).group(1).split()
    options = [a for a in args if a.startswith("-")]
    positional = [a for a in args if not a.startswith("-")]
    remote = positional[0] if positional else "origin"
    refspecs = positional[1:]
    branches = [spec.split(":")[-1] for spec in refspecs]
    return remote, branches, options


def scope_problems(tool, tool_input):
    """Return reasons a call falls outside the approved repository or branch."""
    problems = []
    if tool in SHELL_TOOLS:
        command = str(tool_input.get("command", ""))
        if GH_REPO_SUBCOMMANDS.search(command):
            flag = GH_REPO_FLAG.search(command)
            if flag is None:
                problems.append("gh command does not name a repository with --repo, "
                                "so it would act on whatever repo the working folder points at.")
            elif flag.group(1).strip("'\"") != ALLOWED_REPO:
                problems.append(f"gh command targets {flag.group(1)}, not {ALLOWED_REPO}.")
        if re.search(r"\bgit\s+push\b", command):
            remote, branches, options = push_targets(command)
            if "--all" in options or "--mirror" in options:
                problems.append("git push --all/--mirror pushes every local branch.")
            if not branches:
                current = run_git(["branch", "--show-current"])
                branches = [current] if current else []
            for branch in branches:
                if branch in PROTECTED_BRANCHES:
                    problems.append(f"git push targets protected branch '{branch}'.")
            remote_url = run_git(["remote", "get-url", remote])
            if remote_url and ALLOWED_REPO not in remote_url:
                problems.append(f"remote '{remote}' points at {remote_url}, not {ALLOWED_REPO}.")
    else:
        owner, repo = tool_input.get("owner"), tool_input.get("repo")
        if owner or repo:
            target = f"{owner}/{repo}"
            if target != ALLOWED_REPO:
                problems.append(f"{tool} targets {target}, not {ALLOWED_REPO}.")
        branch = tool_input.get("branch")
        if branch in PROTECTED_BRANCHES:
            problems.append(f"{tool} targets protected branch '{branch}'.")
    return problems


def run_git(args):
    """Return git output for a short summary, or an empty string if unavailable."""
    try:
        result = subprocess.run(["git", *args], cwd=ROOT, capture_output=True,
                                text=True, timeout=5)
        return result.stdout.strip() if result.returncode == 0 else ""
    except (OSError, subprocess.SubprocessError):
        return ""


def trim(text):
    text = text.strip()
    return text if len(text) <= MAX_DETAIL else text[:MAX_DETAIL] + "\n...[truncated]"


def classify(payload):
    """Return (category, description, checklist, details) or None if not gated."""
    tool = str(payload.get("tool_name", ""))
    tool_input = payload.get("tool_input", {}) or {}

    problems = scope_problems(tool, tool_input)
    if problems:
        details = trim("\n".join(problems + ["", f"Approved repository: {ALLOWED_REPO}"]))
        return ("scope", "Target is outside the approved repository or branch.",
                ["Repository and branch are the intended ones",
                 "Approve only if this is deliberate"], details)

    if tool in SHELL_TOOLS:
        command = str(tool_input.get("command", ""))
        for category, pattern, description, checklist in SHELL_GATES:
            if pattern.search(command):
                details = [f"Command: {command}", f"Working directory: {ROOT}"]
                if category == "push":
                    status = run_git(["status", "--short", "--branch"])
                    if status:
                        details.append("git status:\n" + status)
                    log = run_git(["log", "@{u}..HEAD", "--oneline"])
                    if log:
                        details.append("Commits not yet pushed:\n" + log)
                return category, description, checklist, trim("\n".join(details))
        return None

    for category, pattern, description, checklist in MCP_GATES:
        if pattern.search(tool):
            details = [f"Tool: {tool}", "Arguments:", json.dumps(tool_input, indent=2)]
            return category, description, checklist, trim("\n".join(details))

    if tool in WRITE_TOOLS:
        raw_path = str(tool_input.get("file_path", ""))
        if raw_path:
            target = Path(raw_path)
            if not target.is_absolute():
                target = ROOT / target
            if target.exists():
                details = [f"File to overwrite: {target}",
                           f"Current size: {target.stat().st_size} bytes",
                           "The new content replaces the whole file."]
                return ("overwrite",
                        "Overwriting an existing file.",
                        ["This file should be replaced, not edited", "Nothing in it needs keeping"],
                        trim("\n".join(details)))
    return None


def build_reason(category, description, checklist, details):
    lines = [f"HITL approval required [{category}]", description, "", "Proposed action:",
             details, "", "Review checklist:"]
    lines += [f"- [ ] {item}" for item in checklist]
    lines += ["", "Approve to continue, or reject and say what should change."]
    return "\n".join(lines)


def main():
    try:
        payload = json.load(sys.stdin)
        if not isinstance(payload, dict):
            raise ValueError("hook input must be a JSON object")
        match = classify(payload)
    except (ValueError, OSError) as error:
        match = ("unclassified", "The approval gate could not classify this call.",
                 ["Inspect the raw call before approving"], str(error))

    if match is None:
        print(json.dumps({"continue": True}))
        return 0

    category, description, checklist, details = match
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "ask",
            "permissionDecisionReason": build_reason(category, description, checklist, details),
        }
    }))
    return 0


if __name__ == "__main__":
    sys.exit(main())
