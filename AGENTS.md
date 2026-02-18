# Agents

This is the short- and long-term memory file for AI agents working on this project.

## Workflow Rules (Docs-First)

- Before implementation, update docs first to capture intent and planned work.
- Primary flow is:
  1. Backlog idea in `docs/BACKLOG.md` (and `docs/backlog/*.md` when needed)
  2. Move active item into `docs/WORK.md`; add a task that **links to the idea doc** (e.g. `backlog/idea_name.md`)
  3. Iterate implementation and documentation together:
     - add/adjust tasks in `docs/WORK.md`
     - implement or modify code
     - record delivered changes under `Unreleased` in `CHANGELOG.md`, **including a link to the idea doc** (e.g. “Based on: …” per version or per feature)
     - repeat until the **idea** (not just a sub-step) is complete
  4. Stamp a version and release once the full idea loop is complete; then **remove** the task from `WORK.md` (do not leave it struck—WORK is in-progress only, no history).
- `WORK.md` is **only** in-progress: a todo list while an idea is active. Multi-step ideas stay in WORK across phases until fully released.
- Do not clear an idea from `WORK.md` after a partial implementation slice; instead update the active line to the next phase/scope.
- Keep `BACKLOG.md` for queued ideas and `CHANGELOG.md` for done/released work.
- Commit often with small, understandable changesets.
- Reference commit IDs in docs/notes when it helps trace implementation decisions.
- Release hygiene for `CHANGELOG.md`:
  - once a feature loop is verified/done, move its entries from `Unreleased` into the new version section immediately
  - reset `Unreleased` placeholders (`Added/Changed/Fixed`) after the cut
  - update compare links (`[Unreleased]` should point from latest version tag to `HEAD`)
  - prefer using `scripts/changelog_release.py` to cut versions consistently

## Practical Benefit to Preserve

- Docs-first planning makes recovery easier after interruptions or failed runs.
- When uncertain, prefer writing intent in docs before touching code.