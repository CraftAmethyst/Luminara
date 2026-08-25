# Contributing

Development targets the `stable/Trials` branch of [CraftAmethyst/Luminara](https://github.com/CraftAmethyst/Luminara). Changes must preserve Minecraft `1.20.1`, CraftBukkit `v1_20_R1`, and 64-bit Java 17; Forge resolves to the latest promotion unless `-PforgeVersion` is explicitly supplied.

## Before opening a change

- Search existing issues and pull requests.
- Keep one independently reviewable change per commit. Do not squash unrelated fixes together.
- Preserve Forge-only Bukkit compatibility; do not introduce Fabric, NeoForge, or another Minecraft version.
- Prefer synchronous, vanilla-compatible behavior over experimental world, chunk, entity, or persistence optimizations.
- Do not add release, Maven publication, custom upload, or credential-handling infrastructure. The project does not publish releases.

## Build and verify

Select a 64-bit JDK 17 through `JAVA_HOME`, then run from the repository root:

```bash
./gradlew check assembleDistribution verifyDistribution
./gradlew smokeServer
```

The first command must produce `build/distributions/luminara-1.20.1-1.0.15-hotfix.jar` and its `.jar.sha256` file. Forge resolves to the latest promotion by default. A second build invocation is not a repair strategy: do not use the old double-build sequence or `--refresh-dependencies`.

Changes to archive metadata, dependency resolution, or build inputs must also pass:

```bash
./gradlew verifyReproducibleBuild
```

Use `gradlew.bat` instead of `./gradlew` on Windows.

## Source conventions

- Follow the existing style in each module; do not introduce a second convention.
- Do not hand-edit or reformat `arclight-common/src/main/java/io/izzel/arclight/common/mod/metrics/Metrics.java`.
- Treat `arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/generated/` as generated compatibility source. Change its generator or runtime contract rather than applying cosmetic edits.
- Include tests for new observable behavior and regression tests for bug fixes.
- Do not commit generated build output, server state, caches, logs, or IDE metadata.

## Pull requests

Describe the problem, the chosen behavior, compatibility impact, and exact verification performed. Link the issue when one exists. A pull request is ready only when the documented commands pass in one clean invocation and all affected callers, tests, and documentation are updated.

Inherited Arclight code remains subject to the upstream [contribution guidance](https://wiki.izzel.io/s/arclight-docs/doc/contributing-0m2U2kEyC2) and repository license notices.
