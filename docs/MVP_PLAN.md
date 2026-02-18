# MVP Scope

## Must Have
1. Custom Entity: Paperclip Imp
2. Follows player
3. Can be spawned with simple item
4. One example trigger (e.g., diamond ore)
5. Global cooldown for speech
6. Basic line pool system (hardcoded initially)

## Entity Behavior

### Base Behavior
- Follows player at short distance
- Teleports if too far
- Idle animation (subtle wiggle or bounce)
- Small hitbox
- No combat AI

### Voice System (MVP Version)

Rules
- Global cooldown: 6–10 seconds
- Max lines per Minecraft day: 8
- One trigger implemented: found_diamond_ore
- 3–5 diamond lines hardcoded

No:
- No biome tracking
- No rarity tiers yet
- No persistence complexity
- No voice packs yet

⸻

## Day 1 Development Order
1. NeoForge project setup
2. Register entity
3. Model + renderer (simple geometry is fine)
4. Follow AI goal
5. Spawn item
6. Hardcoded trigger + one chat line
7. Add global cooldown logic

If it walks and says one line, MVP is achieved.
