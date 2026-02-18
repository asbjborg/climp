# Command Rod / Adventuring Stick

## Implementation Status

- Phase 1 complete (`fb599b8`): command rod item, log targeting, nearest-Climp assignment, start/completion lines.
- Phase 2 complete (`e03b247`): slow log breaking, return behavior, explicit fail outcomes, and recovery from unreachable targets.
- Phase 3 complete: tree-cluster targeting from clicked log (connected-log scan, safety cap, ground-nearest anchor as first target).
- Phase 3b complete: whole-tree chomp — after breaking the anchor log, continue with remaining logs in the cluster (e.g. ascending Y) until cluster is cleared or cap reached (~100 logs per command).
- Phase 4 complete: in-game runtime config commands for command-rod tuning (`/climp config ...`) without relog.
- Phase 4b complete: command-edited runtime tree config persists per world and reapplies on server start.
- Next (phase 5): capture commanded break drops, carry them with Climp during return, and unload near the requester.

---

## Phase 3 Direction (Tree-Cluster Targeting)

Intent:
- Click any log in a tree, then resolve a smarter target from that tree instead of relying on arbitrary vertical assumptions.

Planned rules:
- Scan connected logs starting from clicked log (including corner/diagonal neighbors) with a strict max-node safety cap (configurable; default 100).
- Select a ground-nearest anchor log from the discovered cluster as the first actionable target.
- Phase 3b: After each log is broken, assign the next log in the cluster (e.g. lowest remaining) until the tree is cleared or break cap (configurable; default 100) is reached.
- Phase 3b reach scaling: in scan-chomp mode, effective task reach grows by +1 block per +1 Y above the anchor log to reduce lingering unreachable crowns.
- Floating-tree start handling: in scan-chomp mode, the anchor distance establishes initial reach baseline so chomping can start even when the anchor is above normal base reach.
- Optional debug mode prints scanned/queued log counts for tuning tree limits.
- If scan exceeds safety cap, fail gracefully with a clear message and do not lock Climp.
- Keep command-task state recoverable at all times (no stuck "busy" state).
- Commanded log drops are carried back and delivered near the player instead of being left at the break position.
- Shift-rightclick with command rod acts as emergency recall: abort current task immediately, return to player, unload carried items.
- Phase 4 runtime tuning commands:
    - `/climp config show`
    - `/climp config set commandTreeScanLimit <int>`
    - `/climp config set commandTreeBreakLimit <int>`
    - `/climp config set commandTreeScanDebugMessages <true|false>`
- Phase 4b persistence:
    - Runtime command edits are saved in world data.
    - Persisted runtime values are automatically reapplied on server start.

---

## Concept

A simple, elegant item that allows the player to direct Climp to perform small, single-target tasks.

The command rod is not automation infrastructure.
It is a momentary delegation tool.

Philosophy:
Climp assists — he does not replace gameplay.

---

## Design Goals

- Reduce early survival time pressure (especially day 1).
- Encourage strategic delegation.
- Preserve core Minecraft progression.
- Keep Climp “moderately helpful, structurally flexible.”

---

## MVP Scope (Post-MVP Feature)

Initial implementation should be minimal:

- Right-click block with rod.
- If block is a log:
    - Climp pathfinds to it.
    - Breaks it slowly.
    - Returns to player.
- One sarcastic line on task start.
- One line on task completion.

No:
- Multi-block harvesting.
- Automation loops.
- Full inventory management/sorting systems.
- Redstone integration.

---

## Behavioral Rules

- Climp can only perform one task at a time.
- Task has a small cooldown after completion.
- Task range limited (e.g., 16 blocks).
- If task fails (mob interruption, path blocked), Climp returns.

Tone example:
> “Manual labor detected. Delegation accepted.”
> “Timber achieved. You are welcome.”

---

## Future Expansion Ideas

- Tune tree cluster scan/break caps via configs as worlds and play styles vary.
- Mine single ore block.
- Fetch dropped item.
- Light area with torches.
- Build small temporary bridge.
- Refuse dangerous tasks:
    - Lava
    - Deep caves
    - Night exploration without light

---

## Technical Notes

Likely requires:

- Custom goal class (e.g., `ClimpTaskGoal`)
- Temporary task state
- Block target storage
- Pathfinding + break animation
- Speech trigger integration

---

## Emotional Layer

The rod reinforces:
- Shared effort.
- Playful teamwork.
- Delegation as strategy.

It should feel like:
“I’ll handle this. You focus.”

Not:
“Why would you ever chop wood yourself?”
