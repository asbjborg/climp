# Work

**In-progress only.** This is the todo list for active work. Tasks reference [BACKLOG.md](BACKLOG.md) and **link to the idea doc** under `docs/backlog/`. When we pick an idea, we add a task here (with that link). When it's fully done—documented in [CHANGELOG.md](../CHANGELOG.md) (with the idea-doc link) and released with a version—we **remove** it from here. WORK holds no history; done work lives in CHANGELOG.

---

## Active Work - [command_rod.md](backlog/command_rod.md)

- [ ] test faster failure behavior. Results:
    - tbd
- [ ] make it fail faster when it can't reach the target instead of waiting for timeout
- [x] test better reach and timeout behavior. results:
    - climp can now reach to 4 blocks above and below where he stands
    - still times out on unreachable targets, could be improved
- [x] give climp better reach, so he can break logs at a distance above and below where he stands
- [x] implement timeout and fallback handling to prevent Climp getting stuck in command-task states.
- [x] test log breaking behavior
    - success path:
        - ground level:
            - post ack. msg.
            - breaks log
            - returns and posts success msg.
        - anything above ground level fails: climp gets stuck in "not available" state
- [x] implement initial log breaking behavior as new task
