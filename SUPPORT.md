# Support Policy

## Supported environment

Support is limited to the current `stable/Trials` branch with Minecraft `1.20.1`, the latest Forge promotion for that Minecraft version, CraftBukkit package `v1_20_R1`, Luminara `1.0.15`, and 64-bit Java 17.

Other Minecraft versions, Fabric, NeoForge, modified forks, and experimental asynchronous world or persistence behavior are outside the supported scope. The project does not publish release binaries; build the distribution from the reviewed source using [README.md](README.md).

## Before filing an issue

1. Reproduce the problem on the supported matrix.
2. Remove unrelated mods and plugins, then identify the smallest combination that still fails.
3. Search [existing issues](https://github.com/CraftAmethyst/Luminara/issues).
4. Collect complete startup and failure logs, reproduction steps, and the output of `/luminara info` when the server starts.
5. Remove credentials, player data, addresses, and other private information from attachments.

Use the repository's bug report form for reproducible Luminara defects. General Minecraft, Forge, mod, plugin, Java, or hosting questions belong with the relevant project or provider. If the same problem occurs on upstream [Arclight](https://github.com/IzzelAliz/Arclight), include that result in the report.

For contribution requirements, see [CONTRIBUTING.md](CONTRIBUTING.md). Report suspected vulnerabilities privately according to [SECURITY.md](SECURITY.md), not through a public support issue.
