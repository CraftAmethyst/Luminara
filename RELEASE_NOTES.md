# Luminara 2.0.0-alpha.3

> Minecraft 1.20.1 | Forge 47.4.0 | Fabric Loader 0.16.9 | Java 17 | Dedicated Server

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### 配置迁移

- Forge 和 Fabric 统一使用 `config/luminara-common.toml`。首次启动时，如果 TOML 文件不存在，Luminara 会从服务端根目录的 `luminara.yml` 导入设置，并保留原文件。
- 旧版的权限转发设置会迁移到 `compatibility.permission-forwarding`。已停止支持的异步存档及优化选项会在迁移时移除。

### 新增

- Forge 和 Fabric 服务端启动时会在后台检查更新。发现与当前 Minecraft 版本和加载器匹配的新版本时，控制台会显示对应的下载链接。正式版只提示正式发布，预发布版也会提示更新的预发布。

### 修复与改进

- 修复 Fabric 在 Windows 旧代码页下的控制台编码问题。彩色横幅、插件日志和包含中文的命令均可正确显示或处理，按回车即可执行命令。
- 修复文件日志中混入的 `mainEx` 和 `Server threadEx` 字样，异常仍会记录堆栈。
- 更新彩色启动横幅，分别显示 Minecraft、实际运行的加载器和 Java 版本，以及 Bukkit 兼容版本。横幅附有 CraftAmethyst 署名和项目仓库地址。
- 服务端统计改用官方 bStats 库，并保留 `bStats/config.yml` 中的设置及服务器 UUID。

### 构建与产物

- 使用 Java 17 运行 `./gradlew build`。Windows 下运行 `gradlew.bat build`。
- 将适用于当前加载器的 JAR 放入服务端的 `mods/` 目录。
  - `luminara-forge-1.20.1-2.0.0-alpha.3.jar`
  - `luminara-fabric-1.20.1-2.0.0-alpha.3.jar`

</details>

## English Release Notes

### Configuration Migration

- Forge and Fabric now both use `config/luminara-common.toml`. On startup, if the TOML file is missing, Luminara imports settings from `luminara.yml` in the server root and keeps the original file.
- The migration moves the old permission forwarding setting to `compatibility.permission-forwarding` and removes retired asynchronous saving and optimization options.

### Added

- Forge and Fabric servers now check for updates in the background at startup. The console prints a download link when it finds a newer release for the current Minecraft version and loader. Stable builds report stable releases. Prerelease builds also report newer prereleases.

### Fixes and Improvements

- Fixed Fabric console encoding on Windows with legacy code pages. The console displays the colored banner and plugin logs correctly, accepts Chinese text in commands, and submits commands with Enter.
- Removed stray `mainEx` and `Server threadEx` text from log files. Exceptions still include stack traces.
- Refreshed the colored startup banner with the Minecraft version, the running loader and Java versions, and the Bukkit compatibility version. The banner credits CraftAmethyst and links to the source repository.
- Switched server metrics to the official bStats library while preserving the settings and server UUID in `bStats/config.yml`.

### Build and Artifacts

- Build with Java 17 using `./gradlew build` or `gradlew.bat build` on Windows.
- Place the JAR for your loader in the server's `mods/` directory.
  - `luminara-forge-1.20.1-2.0.0-alpha.3.jar`
  - `luminara-fabric-1.20.1-2.0.0-alpha.3.jar`
