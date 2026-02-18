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
