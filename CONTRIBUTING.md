# Contributing

Development targets the `stable/Trials` branch of [CraftAmethyst/Luminara](https://github.com/CraftAmethyst/Luminara). Changes must preserve Minecraft `1.20.1`, CraftBukkit `v1_20_R1`, and 64-bit Java 17. The project builds a shared `common` module plus Forge and Fabric loader modules.

## Before opening a change

- Search existing issues and pull requests.
- Keep one independently reviewable change per commit. Do not squash unrelated fixes together.
- Keep shared behavior in `common`; put loader entry points, mixins, services, and metadata in `forge` or `fabric`.
- Preserve both Forge and Fabric startup paths, including Bukkit/Spigot/Paper compatibility.
- Prefer synchronous, vanilla-compatible behavior over experimental world, chunk, entity, or persistence optimizations.
- Do not add release, Maven publication, custom upload, or credential-handling infrastructure. The project does not publish releases.

## Build and verify

Select a 64-bit JDK 17 through `JAVA_HOME`, then run from the repository root:

```bash
./gradlew check
./gradlew :common:test :fabric:build :forge:build
./gradlew assembleForgeMod assembleFabricMod
./gradlew smokeServer smokeFabricServer
```

`common/libs/spigot-1.20.1-mapped-deobf.jar` is a checked-in build input: Spigot CraftBukkit classes remapped to Mojang names. Regenerate it when the Spigot baseline changes:

```bash
./gradlew remapSpigotJar
```

Then copy `build/spigot/spigot-1.20.1-mapped-deobf.jar` over the checked-in file. The task downloads BuildTools and pins Spigot `1.20.1-R0.1-SNAPSHOT` through the repository pattern.

Changes to archive metadata, dependency resolution, or build inputs must also pass:

```bash
./gradlew verifyForgeModDistribution verifyFabricModDistribution
```

Use `gradlew.bat` instead of `./gradlew` on Windows.

## Source conventions

- Follow the existing style in each module; do not introduce a second convention.
- Keep the Gradle group and Java namespace under `org.craftamethyst.luminara`; upstream dependency coordinates and Bukkit/Spigot API package names stay unchanged.
- Do not hand-edit or reformat `common/src/main/java/org/craftamethyst/luminara/common/mod/metrics/Metrics.java`.
- Treat `common/src/main/java/org/craftamethyst/luminara/common/mod/util/remapper/generated/` as generated compatibility source. Change its generator or runtime contract rather than applying cosmetic edits.
- Include tests for new observable behavior and regression tests for bug fixes.
- Do not commit generated build output, server state, caches, logs, or IDE metadata.

## Pull requests

Describe the problem, the chosen behavior, compatibility impact, and exact verification performed. Link the issue when one exists. A pull request is ready only when the documented commands pass in one clean invocation and all affected callers, tests, and documentation are updated.

Inherited Arclight code remains subject to the upstream [contribution guidance](https://wiki.izzel.io/s/arclight-docs/doc/contributing-0m2U2kEyC2) and repository license notices.
