# Command Rod / Adventuring Stick

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
- Inventory management.
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

- Chop small tree clusters (max 5 logs).
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
