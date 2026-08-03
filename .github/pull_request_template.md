## Problem

<!-- What user-visible or maintenance problem does this change solve? Link the issue when one exists. -->

## Change

<!-- Describe the chosen behavior and why it is the smallest complete fix. -->

## Compatibility

- Minecraft: `1.20.1`
- Forge: `47.4.22`
- CraftBukkit package: `v1_20_R1`
- Java: `17`

<!-- Describe effects on Forge mods, Bukkit plugins, Mixins, mappings, configuration, or persisted server data. -->

## Verification

<!-- List the exact commands and scenarios run. Do not check a box that was not exercised. -->

- [ ] `./gradlew check assembleDistribution verifyDistribution`
- [ ] `./gradlew smokeServer`
- [ ] Added or updated a regression test for new observable behavior
- [ ] Updated affected documentation
- [ ] Verified no credentials, server state, logs, caches, or generated build outputs are included

## Commit scope

- [ ] Each commit contains one independently reviewable change.
- [ ] The change does not add a second-build workaround, `--refresh-dependencies`, or publication infrastructure.
- [ ] All affected callers and obsolete paths were migrated or removed.
