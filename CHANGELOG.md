# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Convention:** For each version or feature that came from a backlog idea, link to the idea doc (e.g. `docs/backlog/idea_name.md`) so we keep the tie from shipped work back to the design.

## [Unreleased]

**Based on:** [command_rod.md](docs/backlog/command_rod.md)

### Added
- `command_rod` item (tools tab) with placeholder handheld model.
- Command assignment flow: right-click a log to assign the nearest available Climp a single target task.
- Task speech lines for start/completion (`TASK_START`, `TASK_COMPLETE`).
- Phase 2 command behavior: Climp slowly breaks the targeted log, then returns to the requesting player before completing the task.
- Climp AI now prioritizes an active command target before normal follow behavior.
- Explicit command task outcomes: successful tasks send completion lines, failed tasks send failure lines.
- Task-specific failure variants: unreachable target and target-removed-before-arrival use distinct failure lines (commit: `9acc37a`).
- Timeout and fallback handling to prevent Climp getting stuck in command-task states.
- Fast unreachable-target detection for stalled pathing, so impossible command targets fail sooner.
- Short command-task cooldown after task resolution to reduce command spam.
- Verified in-game: command rod on non-log blocks does nothing.
- Verified in-game: command rod on logs assigns nearest available Climp; Climp sends start + completion lines.
- Verified in-game: when no Climp is available, player receives an explicit unavailable message.

### Changed
- *(nothing yet)*

### Fixed
- *(nothing yet)*

---

## [0.2.0] - 2026-02-18

**Based on:** [spawn_behavior.md](docs/backlog/spawn_behavior.md)

### Added
- First-join spawn-egg gifting flow for Climp, controlled by config.
- World-level `SavedData` tracker to grant the starter spawn egg once per player per world.

### Changed
- `ClimpConfig` now exposes onboarding settings: `giveSpawnEggOnFirstJoin` and `preferredHotbarSlot`.
- Verified behavior in-game: first world join grants one spawn egg, relogging does not grant duplicates.

---

## [0.1.0] - 2026-02-18

### Added
- **Mod bootstrap:** NeoForge 1.21.1 (Java 21), `ClimpMod` + `ClimpModClient`, `ClimpConfig`.
- **Entity:** Custom Climp entity with registration (`ClimpEntityTypes`), small hitbox, no combat AI.
- **Spawning:** Spawn egg and creative tab so Climp can be spawned in-world.
- **Rendering:** Custom geometric model (`ClimpModel`) and entity renderer; metallic placeholder look.
- **Behavior:** Follow-the-player AI; teleport if too far; companion does not push the player (`isPushable = false`).
- **Speech system:** `ClimpSpeechType` (IDLE / HIT), `ClimpSpeechLibrary`, `ClimpSpeechManager` with global cooldown.
- **Voice lines:** Rare idle chat lines and hit-response lines (no diamond-ore trigger yet).
- **Docs:** BACKLOG.md (idea dump), WORK.md (tasks linked to backlog), CHANGELOG.md (this file); README links to all three.

[Unreleased]: https://github.com/asbjborg/climp/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/asbjborg/climp/releases/tag/v0.2.0
[0.1.0]: https://github.com/asbjborg/climp/releases/tag/v0.1.0
