# Agents

This is the short- and long-term memory file for AI agents working on this project.

## Workflow Rules (Docs-First)

- Before implementation, update docs first to capture intent and planned work.
- Primary flow is:
  1. Backlog idea in `docs/BACKLOG.md` (and `docs/backlog/*.md` when needed)
  2. Move active item into `docs/WORK.md`
  3. Iterate implementation and documentation together:
     - add/adjust tasks in `docs/WORK.md`
     - implement or modify code
     - record delivered changes under `Unreleased` in `CHANGELOG.md`
     - repeat until no active tasks remain for the feature
  4. Stamp a version and release once the feature loop is complete
- Treat `WORK.md` as in-progress only (not queued/done buckets).
- Keep `BACKLOG.md` for queued ideas and `CHANGELOG.md` for done/released work.
- Commit often with small, understandable changesets.
- Reference commit IDs in docs/notes when it helps trace implementation decisions.

## Practical Benefit to Preserve

- Docs-first planning makes recovery easier after interruptions or failed runs.
- When uncertain, prefer writing intent in docs before touching code.