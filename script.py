#!/usr/bin/env python3
"""
Claude Code Task Runner — MHS Clubs

Repeatedly invokes Claude Code in headless mode ("claude -p ...") with the
same general "do the next PLAN.md task" prompt, checks PLAN.md between runs
to decide whether to keep going, and stops automatically when:
  - every task in PLAN.md is checked off, or
  - the model's report mentions needing manual/hands-on confirmation
    (e.g. a Setup Checkpoint), or
  - something errors out.

This does NOT touch model internals, temperature, or training — it just
drives Claude Code's existing scriptable interface (the -p / --print flag),
which is the supported way to automate it.

Usage:
    python claude_code_runner.py
"""

import json
import os
import re
import shutil
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

CLAUDE_EXE = shutil.which("claude")
if CLAUDE_EXE is None:
    print("Could not find 'claude' on PATH. Make sure Claude Code is installed and")
    print("that the same terminal/PATH you use to run it manually also works here.")
    sys.exit(1)

# ---------------------------------------------------------------------------
# Config — edit these for your setup
# ---------------------------------------------------------------------------

PROJECT_DIR = Path(r"C:\Users\preco\Documents\github\MHS-Clubs")
PLAN_FILE = PROJECT_DIR / "PLAN.md"
LOG_FILE = PROJECT_DIR / "runner_log.jsonl"

MAX_ITERATIONS = 50           # hard safety cap so it can't run forever unattended
PAUSE_BETWEEN_RUNS_SEC = 5    # small breather between invocations
RUN_TIMEOUT_SEC = 1800        # kill a single run if it hangs past 30 min

# Keep this scoped rather than reaching for --dangerously-skip-permissions.
# acceptEdits auto-approves file writes/edits; Bash calls may still prompt
# depending on your Claude Code version/config — tighten or loosen as needed.
ALLOWED_TOOLS = "Read,Write,Edit,Bash"
PERMISSION_MODE = "acceptEdits"
OLLAMA_MODEL = "rafw007/qwen36-a3b-claude-coder:q4_K_M"  # matches `ollama list` exactly

GENERAL_PROMPT = """Build mode. Read PLAN.md and AGENTS.md/claude.md before doing anything else.

If any Setup Checkpoint or task has been manually verified by me since your last session but
isn't yet marked in PLAN.md, ask me before assuming — otherwise proceed based on what's
currently checked.

TASK SCOPE: Implement ONLY the next unchecked task in PLAN.md, in order, within the first
category that still has unchecked items. Do not skip ahead to a later category. Do not
implement more than one task, even if it seems related or quick.

BEFORE YOU START:
- Identify which folder(s) this task touches, and check their current contents using your
  read/list tools. Do not assume a structure exists — verify what's already there before
  creating or modifying anything.
- If the task requires a decision not explicitly specified in the PRD, make a reasonable
  default choice consistent with standard Kotlin Multiplatform conventions, and state the
  assumption briefly. Do not stop and ask unless it's a genuine blocker (missing credentials,
  no safe default, or a Setup Checkpoint that needs my hands-on confirmation).

WHILE WORKING:
- Write real, functional code — not placeholder comments, empty function bodies, or TODO stubs.
- Follow standard Kotlin/KMP conventions for naming and structure.
- If you modify existing config files, keep them complete and consistent — don't remove or
  break existing declarations while adding new ones.

COMPLETION HONESTY (important):
Only mark a task [x] if it is genuinely complete and functional. If you're uncertain whether
something fully satisfies the task, mark it [~] partial with a note on exactly what's missing.
Do not inflate completion status.

AFTER YOU FINISH:
- Update PLAN.md: check off (or mark partial) only the specific task you completed this
  session. Do not touch any other checkboxes.
- Stop. Do not proceed to the next task automatically, even if it seems efficient to continue.
- Report: what files you created/modified, what you found already in place before starting,
  any assumptions made, and anything that needs my input before continuing (missing tool,
  version conflict, or a Setup Checkpoint requiring hands-on confirmation).
"""

CHECKPOINT_KEYWORDS = [
    "hands-on confirmation",
    "needs your input",
    "needs my input",
    "requires my",
    "setup checkpoint",
    "manual verification",
    "before continuing",
]

# ---------------------------------------------------------------------------


def count_unchecked_tasks(plan_text: str) -> int:
    # Tolerant of "[ ]", "- [ ]", "* [ ]", indented bullets, "[~]" partial
    # markers, and a literal backslash before the bracket (some models write
    # "\[ ]" to escape markdown, which renders as "[ ]" in a preview but is
    # a literal backslash character in the raw file).
    unchecked = re.findall(r"^\s*[-*]?\s*\\?\[\s*\]", plan_text, re.MULTILINE)
    partial = re.findall(r"^\s*[-*]?\s*\\?\[~\]", plan_text, re.MULTILINE)
    return len(unchecked) + len(partial)


def run_claude_once() -> dict:
    cmd = [
        CLAUDE_EXE,
        "-p", GENERAL_PROMPT,
        "--output-format", "json",
        "--allowedTools", ALLOWED_TOOLS,
        "--permission-mode", PERMISSION_MODE,
        "--model", OLLAMA_MODEL,
    ]

    # "ollama launch claude" is just a convenience wrapper that sets these
    # three env vars and then execs the real claude binary — it doesn't
    # support -p itself. Set them directly here instead, so this works
    # without going through that wrapper at all.
    env = os.environ.copy()
    env["ANTHROPIC_BASE_URL"] = "http://localhost:11434"
    env["ANTHROPIC_AUTH_TOKEN"] = "ollama"
    env["ANTHROPIC_API_KEY"] = ""
    # Belt-and-suspenders: also map Claude Code's internal model-tier requests
    # to your local model, in case --model alone doesn't cover every internal
    # call Claude Code makes (e.g. a background/haiku-tier request).
    env["ANTHROPIC_DEFAULT_SONNET_MODEL"] = OLLAMA_MODEL
    env["ANTHROPIC_DEFAULT_OPUS_MODEL"] = OLLAMA_MODEL
    env["ANTHROPIC_DEFAULT_HAIKU_MODEL"] = OLLAMA_MODEL

    try:
        result = subprocess.run(
            cmd,
            cwd=PROJECT_DIR,
            capture_output=True,
            text=True,
            timeout=RUN_TIMEOUT_SEC,
            env=env,
        )
    except subprocess.TimeoutExpired:
        return {"is_error": True, "error": f"Timed out after {RUN_TIMEOUT_SEC}s"}

    if result.returncode != 0:
        return {
            "is_error": True,
            "error": f"Non-zero exit code {result.returncode}",
            "stderr": result.stderr,
        }

    try:
        return json.loads(result.stdout)
    except json.JSONDecodeError:
        return {"is_error": True, "error": "Could not parse JSON output", "raw_stdout": result.stdout}


def log_entry(entry: dict) -> None:
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(json.dumps({**entry, "timestamp": datetime.now().isoformat()}) + "\n")


def main() -> None:
    if not PLAN_FILE.exists():
        print(f"PLAN.md not found at {PLAN_FILE}. Run the plan-generation prompt first.")
        sys.exit(1)

    for i in range(1, MAX_ITERATIONS + 1):
        plan_text = PLAN_FILE.read_text(encoding="utf-8")
        unchecked = count_unchecked_tasks(plan_text)

        if unchecked == 0:
            print("All tasks in PLAN.md are checked off. Nothing left to do.")
            break

        print(f"\n=== Iteration {i} — {unchecked} unchecked task(s) remain in PLAN.md ===")
        result = run_claude_once()
        log_entry(result)

        if result.get("is_error"):
            print(f"Error: {result.get('error')}")
            if result.get("stderr"):
                print(result["stderr"])
            print("Stopping for manual review.")
            break

        response_text = result.get("result", "")
        cost = result.get("total_cost_usd", "?")
        turns = result.get("num_turns", "?")
        print(f"(cost: ${cost}, turns: {turns})")
        print(response_text[:2000])

        if any(kw in response_text.lower() for kw in CHECKPOINT_KEYWORDS):
            print("\n>>> Possible checkpoint or manual step needed. Pausing runner. <<<")
            print("Review the output above, complete any manual step, then re-run this script.")
            break

        time.sleep(PAUSE_BETWEEN_RUNS_SEC)
    else:
        print(f"Reached MAX_ITERATIONS ({MAX_ITERATIONS}) safety cap. Stopping.")


if __name__ == "__main__":
    main()