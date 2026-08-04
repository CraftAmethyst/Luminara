# Luminara

Luminara is a Forge-only Bukkit compatibility layer based on [Arclight](https://github.com/IzzelAliz/Arclight). It runs Forge mods and Bukkit plugins together on one Minecraft server.

## Supported platform

| Component           | Supported version                             |
| ------------------- | --------------------------------------------- |
| Minecraft           | `1.20.1`                                      |
| Forge               | latest Forge promotion for Minecraft `1.20.1` |
| CraftBukkit package | `v1_20_R1`                                    |
| Java                | `17` (64-bit)                                 |
| Luminara            | `1.0.14`                                      |

Other Minecraft versions and Fabric, NeoForge, or hybrid loader configurations are not supported by this repository.

## Capabilities

- Spigot-compatible Bukkit API on Forge.
- Forge mods from `mods/` and Bukkit plugins from `plugins/`.
- Adventure, MiniMessage, signed-chat, and selected Paper API compatibility.
- Localized configuration through the bundled `i18n-config` module.
- Synchronous, vanilla-compatible world persistence defaults.

## Build from source

Use a 64-bit JDK 17 and the checked-in Gradle wrapper. An empty-cache build requires network access to the dependency repositories and to Mojang, Forge, and Spigot build services.
The build resolves the latest Forge promotion for Minecraft `1.20.1` at configuration time. Use `-PforgeVersion=<version>` to reproduce a specific Forge build or to work offline with a previously resolved version.

```bash
./gradlew check
./gradlew assembleDistribution
./gradlew verifyDistribution
```

The build is single-pass; do not run it twice and do not use `--refresh-dependencies` as a workaround. The distribution outputs are:

- `build/distributions/luminara-1.20.1-1.0.14.jar`
- `build/distributions/luminara-1.20.1-1.0.14.jar.sha256`

Verify the checksum with:

```bash
(cd build/distributions && sha256sum -c luminara-1.20.1-1.0.14.jar.sha256)
```

Run the end-to-end Forge mod and Bukkit plugin smoke test with:

```bash
./gradlew smokeServer
```

Use `gradlew.bat` instead of `./gradlew` on Windows.

## Run a server

1. Put `luminara-1.20.1-1.0.14.jar` in a new server directory.
2. Read the [Minecraft EULA](https://aka.ms/MinecraftEULA). If you accept it, create `eula.txt` in that directory containing exactly `eula=true`.
3. Start Luminara from that directory:

```bash
java -jar luminara-1.20.1-1.0.14.jar nogui
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
