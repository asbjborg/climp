# Work

**In-progress only.** This is the todo list for active work. Tasks reference [BACKLOG.md](BACKLOG.md) and **link to the idea doc** under `docs/backlog/`. When we pick an idea, we add a task here (with that link). When it's fully done—documented in [CHANGELOG.md](../CHANGELOG.md) (with the idea-doc link) and released with a version—we **remove** it from here. WORK holds no history; done work lives in CHANGELOG.

---

## Active Work - [command_rod.md](backlog/command_rod.md)

- [ ] test command rejection messaging variants (busy vs cooldown vs no Climp in range). Results:
    - tbd
- [x] implement contextual command rejection messages (busy, cooldown, out-of-range) instead of one generic unavailable message
- [ ] test emergency recall behavior (idle, moving, breaking, returning). Results:
    - tbd
- [ ] implement shift-rightclick emergency recall: abort active task immediately, return to player, and unload any carried drops
- [ ] test carry return behavior. Results:
    - tbd
- [ ] implement log-carry return behavior: collect drops from commanded break and deliver to player on return (do not leave drops at stump)
- [ ] test tree-cluster scan safety cap behavior. Results:
    - tbd
- [ ] test weird/corner-connected logs selection behavior. Results:
    - tbd
- [ ] replace arbitrary vertical reach assumptions with tree-cluster selection rules
- [ ] implement tree-cluster targeting from a clicked log: scan connected logs with safety cap, then select a ground-nearest anchor log as first target
- [ ] test command task cooldown behavior. Results:
    - tbd
- [x] implement command task cooldown (short lockout after finish/fail to prevent spam)
- [x] test failure reason messaging variants. Results:
    - works: target-removed and unreachable cases produce distinct failure lines, both confirmed in-game
- [x] implement task-specific failure reason variants (unreachable vs target removed) for clearer feedback (commit: `9acc37a`)
- [x] test faster failure behavior. Results:
    - unreachable target gives up in about 5 seconds and Climp recovers correctly
- [x] make it fail faster when it can't reach the target instead of waiting for timeout (commit: `e03b247`)
- [x] test better reach and timeout behavior. results:
    - climp can now reach to 4 blocks above and below where he stands
    - still times out on unreachable targets, could be improved
- [x] give climp better reach, so he can break logs at a distance above and below where he stands (commit: `e03b247`)
- [x] implement timeout and fallback handling to prevent Climp getting stuck in command-task states. (commit: `e03b247`)
- [x] test log breaking behavior
    - success path:
        - ground level:
            - post ack. msg.
            - breaks log
            - returns and posts success msg.
        - anything above ground level fails: climp gets stuck in "not available" state
- [x] implement initial log breaking behavior as new task (commit: `e03b247`)
