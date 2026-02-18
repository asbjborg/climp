# Agents

This is the short- and long-term memory file for AI agents working on this project.

## Workflow Rules (Docs-First)

- Before implementation, update docs first to capture intent and planned work.
- Primary flow is:
  1. Backlog idea in `docs/BACKLOG.md` (and `docs/backlog/*.md` when needed)
  2. Create/update GitHub issue(s) for active work; each issue should include a link to the relevant backlog doc
  3. Iterate implementation and documentation together:
     - track status/progress in the GitHub issue(s)
     - implement or modify code
     - record delivered changes under `Unreleased` in `CHANGELOG.md`, **including a link to the idea doc** (e.g. “Based on: …” per version or per feature)
     - repeat until the **idea** (not just a sub-step) is complete
  4. Stamp a version and release once the full idea loop is complete; then close related GitHub issue(s).
- Active task queue lives in GitHub issues: https://github.com/asbjborg/climp/issues
- Keep `BACKLOG.md` for queued ideas and `CHANGELOG.md` for done/released work.
- Commit often with small, understandable changesets.
- Commit cadence (high priority):
    - commit after each meaningful code/doc change
    - commit after each test-result-driven update (including WORK/CHANGELOG note updates)
    - prefer many small commits over large mixed commits
    - if uncertain, commit sooner rather than later
- Release hygiene for `CHANGELOG.md`:
    - once a feature loop is verified/done, move its entries from `Unreleased` into the new version section immediately
    - reset `Unreleased` placeholders (`Added/Changed/Fixed`) after the cut
    - update compare links (`[Unreleased]` should point from latest version tag to `HEAD`)
    - prefer using `scripts/changelog_release.py` to cut versions consistently
    - prefer frequent small commits during implementation; when a feature loop is declared done, combine the final code/docs updates with the changelog version stamp in one completion commit so `git blame` on `CHANGELOG.md` maps directly to shipped files
    - for active issue-driven work, append the GitHub issue reference (e.g. `(#9)`) in each relevant `Unreleased` bullet
    - in `Unreleased`, prefer additive wording for in-progress work; avoid transitional phrasing like "now", "no longer", or "changed from X to Y" unless documenting a true revision to already-documented released behavior
- Commit message style:
    - transition phrasing is encouraged in commit messages (e.g. "now", "no longer", "switch to", "replace X with Y") because commits describe deltas between snapshots
    - use commit messages to explain "what changed since the previous commit" and why

## Practical Benefit to Preserve

- Docs-first planning makes recovery easier after interruptions or failed runs.
- When uncertain, prefer writing intent in docs before touching code.
