# Climp

Climp is a small metallic companion that follows you through your Minecraft world, offering rare commentary, gentle nudges, and sarcastic observations.

He does not assign quests.

He notices things.

A sarcastic anthropomorphic paperclip companion for Minecraft (NeoForge 1.21.1).

This is not a quest mod.
This is not an automation mod.

This is a companion.

Optional flavor tagline:
- "Climp. Moderately helpful. Structurally flexible."
- "A paperclip forged for adventure."

## Technical Note

Use a stable base package namespace from the beginning:
- `com.asbjborg.climp`
- `dk.asbjborg.climp`

## Features (MVP)
- Custom companion entity
- Follows player
- Rare voice lines
- Global cooldown to prevent spam
- First implemented trigger: diamond ore discovery

## Philosophy

Climp is designed to:
- Encourage exploration
- Lighten tense moments
- Provide subtle companionship
- Make early survival more memorable

Lines are intentionally rare to preserve their impact.

## Installing in your pack

No CurseForge—just drop the mod into your instance:

```bash
./scripts/build_mod.sh
```

Then copy `dist/climp-<version>.jar` into your Minecraft instance’s `mods/` folder. NeoForge 1.21.1 required.

## Docs
- **[docs/BACKLOG.md](docs/BACKLOG.md)** — Backlog (idea dump).
- **[Issues](https://github.com/asbjborg/climp/issues)** — Active tasks and testing queue.
- **[CHANGELOG.md](CHANGELOG.md)** — Release history (Keep a Changelog).

## Roadmap
- Exploration awareness
- Biome tracking
- Rare/legendary lines
- Emotional state system
- Scavenger hunt triggers
- Cosmetic upgrades
