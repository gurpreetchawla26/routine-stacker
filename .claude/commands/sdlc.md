---
description: Execute the active SDLC phase, then remind the user to review
argument-hint: [optional note]
---

Execute the active SDLC phase for `gurpreetchawla26/routine-stacker`. Extra context: $ARGUMENTS

1. Read `.claude/sdlc_state.json`. Use `active_phase` to find the current phase in `phases`. If the workflow is complete, say so and stop.
2. Adopt the phase's `agent` persona by following the matching file in `.claude/agents/`.
3. Address any `feedback` entries for this phase.
4. Write or update only the phase's `artifacts`. Writes to other phases are denied by the phase gate.
5. Summarize the artifacts changed and the key decisions.
6. When the artifact is complete, finish with this reminder:

   > Phase `<active_phase>` is ready. Run `python .claude/hooks/scripts/phase_gate.py review` to approve, decline, or give feedback.

Never approve the phase or run the review command yourself.
