# Luminara

[简体中文](README_zh.md)

中国 / 简体中文用户请加入 [CraftAmethyst 社区 QQ 交流群](https://qm.qq.com/q/u3Dylx2ls6)

# 永久停更 Permanently discontinued

Discord: https://discord.gg/H7RqfGCa

An Arclight fork, aiming to make more optimizations and improvements on Arclight (1.20.1)

> Any issues encountered while using this server software should be reported in this project's Issues, not in the
> Arclight project Issues!

## Features

- **Strong Compatibility** - Supports Bukkit/Spigot plugins and Forge mods running simultaneously
- **High Performance** - Asynchronous world saving, chunk optimization...
- **Easy to Use** - Simple installation and usage
- **Velocity Support** - Supports Velocity Modern forwarding enabling cross-server functionality

## Main Maintained Version

> **Currently Main Maintained Version: Minecraft 1.20.1**
>
> - **Forge Version**: 47.4.16
> - **Stability**: Good
> - **Plugin Compatibility**: Average, Spigot only
> - **Mod Compatibility**: Excellent

## Download

### Stable Versions

- [GitHub Releases](https://github.com/QianMo0721/Luminara/releases) - Recommended for production environments

### Development Versions

- [Daily Build Versions](https://github.com/QianMo0721/Luminara/actions/workflows/gradle.yml?query=branch%3ATrials) *(
  Requires
  GitHub login)*

### Self-Build

- Clone this project locally `git clone -b <branch> https://github.com/QianMo0721/Luminara.git`
- Run `./gradlew cleanBuild remapSpigotJar idea --no-daemon -i --stacktrace --refresh-dependencies` for configuration
- Run `./gradlew build collect` to build the project
- After building, the jar file is located in the `./build/libs` directory

## Installation and Usage

1. **Download** the jar file
2. **Start the server**:

   ```bash
   java -jar luminara.jar nogui
   ```

   > The `nogui` parameter will disable the server control panel
   >
3. Before each update, replace the old JAR file with the new one, then delete the .arclight folder. Otherwise, certain fixes will not take effect!

## Incompatibilities

- May not be compatible with some optimization mods.
- Incompatible with all optimized Bukkit plugins

## Support and Help

### Documentation

- [Arclight Documentation](https://wiki.izzel.io/s/arclight-docs) - Detailed usage guides and configuration instructions
- [To-Do List](TODO.md)

### Issue Reporting

- [Submit Bug](https://github.com/QianMo0721/Luminara/issues/new/choose) - Report problems here
- [Discussion Forum](https://github.com/QianMo0721/Luminara/discussions) - Ask questions and discuss
- Do not report issues with this server software to Arclight!

## License

This project is open source under the [GPL v3](LICENSE) license.
