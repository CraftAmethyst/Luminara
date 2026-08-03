# Luminara

A Bukkit-compatible server layer that runs on Minecraft Forge. Luminara is a fork of [Arclight](https://github.com/IzzelAliz/Arclight), letting you run **Forge mods and Bukkit plugins side by side** on a single server.

## Highlights

- **Bukkit API on Forge** — full implementation of the Spigot API (`v1_20_R1`) for Minecraft **1.20.1** (Forge **47.4.16**, Java **17**).
- **Mod + Plugin coexistence** — drop Forge mods into `mods/` and Bukkit plugins into `plugins/` and run them together.
- **Adventure support** — bundled with Adventure API 4.17.0 including MiniMessage, Gson, Legacy and Plain text serializers.
- **Partial Paper API** — selected Paper APIs and improvements are implemented, e.g. Paper Adventure integration, signed chat messages, and Paper-style optimizations such as the chunk optimizer and world creation optimizer.
- **Installable** — ships with a Forge installer that supports multiple download mirrors for network-restricted environments.
- **i18n-ready** — localized configuration through the bundled `i18n-config` module.

## Requirements

- Java 17 or newer
- Gradle 8.x (the included `gradlew` wrapper is preferred)

## Building from source

```bash
./gradlew build
```

On Windows, use `gradlew.bat` instead.

The build produces the server artifacts under `build/libs`. Due to a MixinGradle quirk, the build may need to be run twice for a fully correct jar — this is also how the CI pipeline (`appveyor-19.yml`) handles it:

```bash
./gradlew build
./gradlew build collect
```

## Usage

1. Build or download the Luminara server jar.
2. Run the installer to set up a server directory, or place the jar in your server directory together with Forge's libraries.
3. Launch the server with `java -jar` and start the server the same way you would any Forge server.
4. Put Forge mods in `mods/` and Bukkit plugins in `plugins/`.

## Documentation & Support

- Contribution guidelines: [CONTRIBUTING.md](CONTRIBUTING.md)
- Code of conduct: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- Upstream project: [IzzelAliz/Arclight](https://github.com/IzzelAliz/Arclight)

## License

Luminara is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE).
