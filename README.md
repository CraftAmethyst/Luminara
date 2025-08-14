# Luminara

[简体中文](/README_zh.md)

> This server software is an Arclight Fork. Please report any issues encountered while using this server software in *
*this project's Issue tracker**. Do **not** report them in the Arclight project's Issue tracker!

## ❓ What is This?

Luminara is a hybrid server software that implements Bukkit/Spigot/Paper APIs on a Forge foundation through Mixin (
Forge+Paper), similar to Mohist/Thermos/MCPC+.

## ✨ Features

- 🔧 **High Compatibility** - Supports simultaneous operation of Bukkit/Spigot/select Paper plugins alongside Forge mods
- 🚀 **High Performance** - Incorporates optimizations from Paper
- 🛠️ **Easy to Use** - Simple installation and operation
- 🌐 **Velocity Support** - Supports Velocity Modern forwarding for cross-server functionality

## 🎯 Primary Maintenance Version

> **Currently maintained version: Minecraft 1.20.1**
>
> - **Forge Version**: 47.4.6
> - **Plugin Compatibility**: Moderate (supports Bukkit/Spigot and select Paper plugins)
> - **Mod Compatibility**: Good

## 📥 Downloads

### Stable Releases

- [GitHub Releases](https://github.com/QianMoo0121/Luminara/releases) - Official releases recommended for production
  environments (excellent stability). PRE releases are for early access (lower stability than official releases).

### Development Builds

- [Nightly Builds](https://github.com/QianMoo0121/Luminara/actions/workflows/gradle.yml?query=branch%3ATrials) _(GitHub
  login required, lower stability than PRE releases)_

### Building from Source

- Clone the repository locally:  
  `git clone -b <branch> https://github.com/QianMoo0121/Luminara.git`
- Configure the project:  
  `./gradlew cleanBuild remapSpigotJar idea --no-daemon -i --stacktrace --refresh-dependencies`
- Build the project:  
  `./gradlew build collect`
- The compiled JAR file will be located in the `./build/libs` directory

## 🚀 Installation & Usage

1. **Download** the JAR file
2. **Start the server**:

   ```bash
   java -jar luminara.jar nogui
   ```

   > The `nogui` parameter disables the server GUI panel

## ❌ Incompatibilities

- Incompatible with **all optimization mods/plugins**
  > _(Paper's partial optimizations are already included)_

## 📚 Support & Resources

### 📖 Documentation

- [Arclight Documentation](https://wiki.izzel.io/s/arclight-docs) - Detailed usage guides and configuration instructions
- [To-Do List](TODO.md)

### 🐛 Issue Reporting

- [Report Bugs](https://github.com/QianMoo0121/Luminara/issues/new/choose) - Submit encountered issues here
- [Discussions](https://github.com/QianMoo0121/Luminara/discussions) - For questions and general discussions
- **Do NOT report issues with this server software to Arclight!**

## 📄 License

This project is open-source under the [GPL v3](LICENSE) license.
