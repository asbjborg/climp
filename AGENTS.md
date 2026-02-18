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
- `WORK.md` format preference:
    - use a single active header in the form: `## Active Work - [idea_doc.md](backlog/idea_doc.md)`
    - track work as an interleaved checklist of implementation tasks and test tasks
    - keep test outcomes directly under the related checklist item using `Results:` bullets
    - mark completed steps with `[x]`, keep pending steps as `[ ]`
    - do not use separate "backlog tasks" sections inside `WORK.md`
    - ordering rule: reverse chronological by insertion (new tasks are added at index 0 / top)
    - do not reorder or move existing tasks when updating notes/results; update them in place
    - for implementation-driven items, place the validation/check item immediately above the implementation item (bottom-up execution when reading)
    - include commit IDs on task lines or directly below them when relevant (for traceability)
    - expected cadence: commit implementation first, then do a tiny WORK update commit that records the hash on the related task
- Keep `BACKLOG.md` for queued ideas and `CHANGELOG.md` for done/released work.
- Commit often with small, understandable changesets.
- Commit cadence (high priority):
    - commit after each meaningful code/doc change
    - commit after each test-result-driven update (including WORK/CHANGELOG note updates)
    - prefer many small commits over large mixed commits
    - if uncertain, commit sooner rather than later
- Reference commit IDs in docs/notes when it helps trace implementation decisions.
- Release hygiene for `CHANGELOG.md`:
    - once a feature loop is verified/done, move its entries from `Unreleased` into the new version section immediately
    - reset `Unreleased` placeholders (`Added/Changed/Fixed`) after the cut
    - update compare links (`[Unreleased]` should point from latest version tag to `HEAD`)
    - prefer using `scripts/changelog_release.py` to cut versions consistently
    - in `Unreleased`, prefer additive wording for in-progress work; avoid transitional phrasing like "now", "no longer", or "changed from X to Y" unless documenting a true revision to already-documented released behavior
- Commit message style:
    - transition phrasing is encouraged in commit messages (e.g. "now", "no longer", "switch to", "replace X with Y") because commits describe deltas between snapshots
    - use commit messages to explain "what changed since the previous commit" and why

## Practical Benefit to Preserve

- Docs-first planning makes recovery easier after interruptions or failed runs.
- When uncertain, prefer writing intent in docs before touching code.
