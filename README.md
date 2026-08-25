# Luminara

[简体中文](README_sc.md)

Luminara is a Forge-only Bukkit compatibility layer based on [Arclight](https://github.com/IzzelAliz/Arclight). It runs Forge mods and Bukkit plugins together on one Minecraft server.

> Goal: Implement more Paper APIs, maximize compatibility with mods and plugins, and provide more customization options.

## Supported platform

| Component           | Supported version                             |
| ------------------- | --------------------------------------------- |
| Minecraft           | `1.20.1`                                      |
| Forge               | latest Forge promotion for Minecraft `1.20.1` |
| CraftBukkit package | `v1_20_R1`                                    |
| Java                | `17` (64-bit)                                 |
| Luminara            | `1.0.15-hotfix`                               |

Other Minecraft versions and Fabric, NeoForge, or hybrid loader configurations are not supported by this repository.

## Capabilities

- Spigot-compatible Bukkit API on Forge.
- Forge mods from `mods/` and Bukkit plugins from `plugins/`.
- Adventure, MiniMessage, signed-chat, and selected Paper API compatibility.
- Localized configuration through the bundled `i18n-config` module.
- Synchronous, vanilla-compatible world persistence defaults.
- Works perfectly in the modpacks [ATM9](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9) and [Closing Song](https://www.mcmod.cn/modpack/1133.html)

## Known Incompatible Mods

- [ServerCore](https://modrinth.com/mod/servercore)

  > Most of its optimization methods originate from Spigot or Paper. This server is itself a Forge + Spigot + Paper API server, so forcing it in will cause abnormal behavior or crashes, and thus will never be compatible

- [Sinytra Connector](https://modrinth.com/mod/connector)

  > Are you crazy? Forge + Fabric + Bukkit + Spigot + Paper API = a bunch of shit, never be compatible!

- [C2ME Forge](https://www.curseforge.com/minecraft/mc-mods/concurent-chunk-management-engine-forge/d)

  > Since it's closed-source, we can't read its source code to fix it.

  > You can try [FastChunkGen](https://www.curseforge.com/minecraft/mc-mods/fastchunkgen)

## Known Incompatible Plugins

- Any plugin that claims to optimize server performance (e.g., LaggRemover)

  > Most of these plugins offer fake optimizations — they either don't help performance or actively harm it

- Any anti-cheat plugin (e.g., GrimAC, Matrix, Vulcan)

  > Anti-cheat plugins sometimes misjudge player interactions with modded items (since these plugins only detect vanilla behavior)

- 70% of plugins that use the Paper API

  > This server supports only a handful of Paper APIs — just over a dozen commonly used ones — so you may encounter missing features, abnormal behavior, or even crashes. We are currently working on implementing more Paper APIs

  > Some plugins offer an alternative "Spigot API" version — that is the best compatibility path

## How Can I Maximize Optimization on This Server?

- If you want more aggressive performance optimization, I recommend our downstream project [PRTS-SERVER](https://github.com/ElainAwa/PRTS-SERVER)

  > PRTS-SERVER aims to implement more performance optimization patches while maintaining compatibility as much as possible. These patches come from [ServerCore](https://modrinth.com/mod/servercore), [Very Many Players](https://modrinth.com/mod/vmp-forge), [Paper](https://papermc.io/), [Mohist](https://www.mohistmc.com/), and others

  > Note that this is only suitable for small to medium modpacks! For large modpacks like ATM9, we recommend staying on this server instead, because large modpacks often require more compatibility

- If you want to optimize a large modpack, I recommend some common optimization mod combinations (all mods mentioned below are compatible with Luminara)

  > For most modpacks, just three mods can significantly reduce MSPT: [ModernFix](https://modrinth.com/mod/modernfix), [FerriteCore](https://modrinth.com/mod/ferrite-core), and [Radium](https://modrinth.com/mod/radium) — Luminara is compatible with all three

  > For modpacks that involve heavy exploration, two mods can dramatically speed up chunk generation: [FastNoise](https://modrinth.com/mod/zfastnoise) and [FastChunkGen](https://www.curseforge.com/minecraft/mc-mods/fastchunkgen)

## Build from source

Use a 64-bit JDK 17 and the checked-in Gradle wrapper. An empty-cache build requires network access to the dependency repositories and to Mojang, Forge, and Spigot build services.
The build resolves the latest Forge promotion for Minecraft `1.20.1` at configuration time. Use `-PforgeVersion=<version>` to reproduce a specific Forge build or to work offline with a previously resolved version.
Because the build tracks the latest promotion, a new Forge release ships artifacts that are not yet covered by Gradle dependency verification (`gradle/verification-metadata.xml`), which fails the build during configuration. Regenerate the verification metadata with:

```bash
./gradlew :arclight-common:tasks --write-verification-metadata sha256
```

```bash
./gradlew check
./gradlew assembleDistribution
./gradlew verifyDistribution
```

The build is single-pass; do not run it twice and do not use `--refresh-dependencies` as a workaround. The distribution outputs are:

- `build/distributions/luminara-1.20.1-1.0.15-hotfix.jar`
- `build/distributions/luminara-1.20.1-1.0.15-hotfix.jar.sha256`

Verify the checksum with:

```bash
(cd build/distributions && sha256sum -c luminara-1.20.1-1.0.15-hotfix.jar.sha256)
```

Run the end-to-end Forge mod and Bukkit plugin smoke test with:

```bash
./gradlew smokeServer
```

Use `gradlew.bat` instead of `./gradlew` on Windows.

## Run a server

1. Put `luminara-1.20.1-1.0.15-hotfix.jar` in a new server directory.
2. Read the [Minecraft EULA](https://aka.ms/MinecraftEULA). If you accept it, create `eula.txt` in that directory containing exactly `eula=true`.
3. Start Luminara from that directory:

```bash
java -jar luminara-1.20.1-1.0.15-hotfix.jar nogui
```

The launcher exits without prompting when `eula.txt` is missing or does not contain the exact accepted value. It never accepts the EULA on your behalf.

Add Forge `1.20.1` mods to `mods/` and Bukkit plugins compatible with `v1_20_R1` to `plugins/`. Back up the server before changing either set.

## Distribution policy

This repository has no GitHub Release, Maven publication, or custom binary publication workflow. CI may retain verification artifacts for seven days; those artifacts are not releases. Build the distribution from the reviewed `stable/Trials` branch when you need a binary.

## Project guidance

- [Contributing](CONTRIBUTING.md)
- [Code of conduct](CODE_OF_CONDUCT.md)
- [Security policy](SECURITY.md)
- [Support policy](SUPPORT.md)
- [Upstream Arclight project](https://github.com/IzzelAliz/Arclight)

## License

Luminara is licensed under the [GNU General Public License v3.0](LICENSE). Arclight attribution and upstream notices remain applicable to inherited code.
