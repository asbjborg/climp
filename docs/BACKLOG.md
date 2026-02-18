# Backlog

High-level idea index. Keep each entry short here; detailed thinking lives in dedicated docs under `docs/backlog/`. When something becomes active work, we track it in GitHub Issues.

---

## Backlog Structure Convention

The backlog acts as a high‑level idea index only.

- Each idea here should be represented as a short header with 1–3 bullet points.
- If an idea grows beyond quick notes, it gets its own dedicated document.

### Dedicated Idea Docs

When an idea needs design thinking, technical breakdown, or phased planning:

1. Create a new file under:
   `/docs/backlog/{idea_name}.md`
2. Keep the filename lowercase with underscores (e.g. `command_rod.md`).
3. Add a short link under the idea header in this file.

Example:

```
## Command rod / adventuring stick
See: [command_rod.md](backlog/command_rod.md)
```

The backlog remains lightweight and scannable.
Deep thinking lives in dedicated files.
GitHub Issues track active implementation.

---

## Command rod / adventuring stick
See: [command_rod.md](backlog/command_rod.md)

A cane or stick item you carry (like a walking stick irl). Used to "send Climp off" to do simple tasks. Reduces day‑1 time pressure before first night.

- **First use case:** send Climp to chop down a tree so you can focus on shelter/food.
- Post‑MVP; fits "moderately helpful, structurally flexible."

---

*Add more ideas below. When we pick one up, create/update a GitHub issue and link to this idea doc there—and keep that link when we record the work in [CHANGELOG.md](../CHANGELOG.md).*

---

## Spawn behavior (starter item config)
See: [spawn_behavior.md](backlog/spawn_behavior.md)

Config‑controlled behavior that determines whether Climp’s spawn egg is granted automatically on first world join.

- Boolean toggle in config (default: true).
- Grant spawn egg once per player per world.
- Prefer placing in hotbar slot if available.
- Future: replace spawn egg with craftable summoner item.

---

## Natural Progression Roadmap (Fresh World Testing)
See: [fresh_world_progression.md](backlog/fresh_world_progression.md)

Roadmap for phase-based feature development during fresh-world survival sessions.
