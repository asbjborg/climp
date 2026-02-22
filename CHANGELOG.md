# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Convention:** For each version or feature that came from a backlog idea, link to the idea doc (e.g. `docs/backlog/idea_name.md`) so we keep the tie from shipped work back to the design.

## [Unreleased]

### Added
- *(nothing yet)*

### Changed
- *(nothing yet)*

### Fixed
- *(nothing yet)*

---

## [0.4.1] - 2026-02-18

### Added
- Additional voice lines: hit (2), task complete (2), task failed unreachable (2), task failed target removed (2).

### Changed
- *(nothing yet)*

### Fixed
- *(nothing yet)*

---

## [0.4.0] - 2026-02-22

### Added
- Voice playback for Climp speech: one `.ogg` file per line in `assets/climp/sounds/`. Speech plays at the entity position in addition to chat.
- Climp never repeats the same line twice in a row; last-used line per speech type is excluded when picking the next.
- Single source of truth: `ClimpSpeechLibrary` maps each line’s text directly to its sound file name.
- `scripts/generate_va.py` to generate voice lines via ElevenLabs: pick category and line, preview, then save to sounds and wire into the mod.
- `scripts/mp3_to_ogg.sh` to batch-convert MP3 files in the sounds folder to OGG.

### Changed
- *(nothing yet)*

### Fixed
- *(nothing yet)*

---

## [0.3.1] - 2026-02-18

### Added
- Runtime command-rod tuning commands: `/climp config show` and `/climp config set ...` for scan limit, break limit, and scan debug toggle. (#9)
- Operator-gated config editing in-game (permission level 2+) with immediate feedback on updated values. (#9)
- Runtime command-edited tree config now persists per world using `SavedData` and survives restarts. (#10)
- Persisted runtime tree config is automatically reapplied on server startup to keep effective values in sync. (#10)
- Commanded-break drop carry flow: Climp captures log drops from command tasks, carries them during return, and unloads near the requester. (#3)
- Floating-tree scan starts now inherit initial reach from anchor distance so Climp can begin chomping when the lowest scanned log is above base reach. (#3)

### Changed
- *(nothing yet)*

### Fixed
- *(nothing yet)*

---

## [0.3.0] - 2026-02-18

**Based on:** [command_rod.md](docs/backlog/command_rod.md)

### Added
- `command_rod` item (tools tab) with placeholder handheld model.
- Command assignment flow: right-click a log to assign the nearest available Climp a tree-cluster task queue.
- Task speech lines for start/completion (`TASK_START`, `TASK_COMPLETE`).
- Phase 2 command behavior: Climp slowly breaks the targeted log, then returns to the requesting player before completing the task.
- Climp AI now prioritizes an active command target before normal follow behavior.
- Explicit command task outcomes: successful tasks send completion lines, failed tasks send failure lines.
- Task-specific failure variants: unreachable target and target-removed-before-arrival use distinct failure lines (commit: `9acc37a`).
- Timeout and fallback handling to prevent Climp getting stuck in command-task states.
- Fast unreachable-target detection for stalled pathing, so impossible command targets fail sooner.
- Short command-task cooldown after task resolution to reduce command spam.
- Contextual command rejection messages that distinguish busy, cooldown, and no-Climp-in-range cases.
- Shift-rightclick emergency recall that aborts an active command task and immediately returns Climp to the player.
- Tree-cluster targeting from clicked logs: connected-log scan (including corner connections), safety cap, and ground-nearest anchor selection.
- Phase 3b tree chomp flow: after the anchor log breaks, Climp chains through remaining logs in the discovered cluster (up to 100 logs total per command) before returning.
- Command-task scan safety limit aligned with the per-command tree-chomp cap at 100 logs.
- In tree scan-chomp mode, effective command reach scales by +1 per +1 Y above the cluster anchor to improve tall-tree crown cleanup.
- Command-rod tree behavior is now configurable: `commandTreeScanLimit` (default 100) and `commandTreeBreakLimit` (default 100).
- Optional `commandTreeScanDebugMessages` config prints scanned/queued log counts for tree-size tuning.
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

[Unreleased]: https://github.com/asbjborg/climp/compare/v0.4.1...HEAD
[0.4.1]: https://github.com/asbjborg/climp/releases/tag/v0.4.1
[0.4.0]: https://github.com/asbjborg/climp/releases/tag/v0.4.0
[0.3.1]: https://github.com/asbjborg/climp/releases/tag/v0.3.1
[0.3.0]: https://github.com/asbjborg/climp/releases/tag/v0.3.0
[0.2.0]: https://github.com/asbjborg/climp/releases/tag/v0.2.0
[0.1.0]: https://github.com/asbjborg/climp/releases/tag/v0.1.0
