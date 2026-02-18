# Spawn Behavior (Starter Item Config)

## Purpose

Control how Climp is introduced into a new world.

The goal is smooth onboarding without being intrusive.

---

## Config Design

Config boolean:

giveSpawnEggOnFirstJoin = true

Optional future settings:

preferredHotbarSlot = 0
giveOnlyInNewWorlds = false

---

## MVP Behavior

On Player Logged In:

If:
- Server side
- Config enabled
- Player has not already received the spawn egg in this world

Then:
- Grant Climp spawn egg.
- Attempt to place in preferred hotbar slot.
- Fallback to first empty slot.
- If inventory full, drop at player feet.
- Mark as given for this world.

---

## World-Specific Logic

Must be:
Once per player per world.

Recommended approach (MVP and beyond):
- Use world `SavedData` to track recipients per world save.
- Store per-player identity in that world-level data (UUID).
- On login, check `SavedData`; grant only if absent, then record as granted.

Why this is preferred:
- Cleaner multiplayer semantics.
- Avoids brittle key naming and seed-coupled logic.
- Keeps per-world onboarding logic centralized.

---

## UX Goals

- Feels intentional, not intrusive.
- Does not spam multiple eggs.
- Does not override existing inventory.
- Works in both singleplayer and multiplayer.

---

## Future Direction

Replace spawn egg with:

- Craftable summoner item.
- Intro book explaining Climp.
- One-time intro cinematic line.
- Optional config to disable automatic gifting.

---

## Emotional Framing

When egg is given, optional intro line:

> “Deployment successful.”
> “I appear to exist now.”

Introduction should feel magical, not technical.
