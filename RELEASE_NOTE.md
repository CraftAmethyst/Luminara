# Luminara 1.0.15

> Minecraft 1.21.1 | Fabric 0.19.3 | NeoForge 21.1.248 | Java 21 | Dedicated Server

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### BREAKING CHANGES

- 发行物改为标准 Fabric/NeoForge 服务端模组，从 `mods/` 加载；旧版启动器仅作为已弃用的回退方案保留。
- Bukkit 插件仍位于 `plugins/`；Fabric 额外需要 Fabric API 和 `fabric-permissions-api`。

### Added

- 新增标准 Fabric/NeoForge 模组分发，包含加载器入口点、元数据、Mixin 配置和版本信息。
- 新增分发装配与校验：加载器元数据、Mixin 配置、重复类、启动器残留和 NeoForge jar-in-jar 库。
- 新增 NeoForge 对 SnakeYAML 的 jar-in-jar 打包，以及开放式依赖元数据和分发契约测试。
- 新增原生服务器回归夹具，覆盖插件加载、命令、调度、事件、关闭、语言资源和 JUL 日志。
- 新增 C2ME 区块系统、VanillaBackport 投掷鸡蛋和 LuckPerms 权限注入兼容。
- 新增 Paper 风格的未加版本号 `CraftSpawnCategory` 门面，供 Multiverse-Core 等插件调用。

### Fixed

- 统一 Bukkit/JUL 日志到 Log4j 格式，替换 JUL 默认的 `SimpleFormatter` 输出。
- 修复内置详细/简单 Log4j 配置未生效的问题，恢复 ANSI 颜色和原生控制台字符集，并修正异常格式化器多余的 `Ex`。
- 修复 Fabric 控制台输入与命令补全，解决 Jansi 类路径冲突。
- 修复启动横幅重复输出以及错误的版本、构建时间，并修复发布名称渲染、ASCII 高亮和 UTF-8 语言输出。
- 修复 `org.bukkit.craftbukkit.v.*` 重映射（CMILib `CraftLegacy`、CMI `SkullMeta`）。
- 修复 Paper 插件的 Mojang 名称反射，恢复 Multiverse-Core 的生成类别映射。
- 修复 Fabric Mixin 冲突（铁砧、蜂巢、投掷鸡蛋、C2ME ticket）、`Text.iterator()` 加入崩溃和被改写的 Minecraft 版本。
- 修复关闭挂起、嵌入式 JAR 不刷新、Fabric 引导类重复和插件类缓存跨版本复用。
- 修复 NeoForge 的 SnakeYAML 包冲突和过期安装程序制品；映射生成不再依赖 Forge 或 SRG provider 缓存。

### Changed

- 版本号更新为 `1.0.15`。
- 分发制品和元数据统一使用 Luminara 名称，并包含可重现的 Git 修订信息。
- 共享引导与运行时代码移入 `arclight-common`，Fabric 和 NeoForge 提供各自的入口点。
- 构建流程重新组织，包含优化的加载器构建、分发校验和 Linux 校验和；映射按加载器直接生成。

### 兼容性

- Minecraft：1.21.1
- Fabric：0.19.3
- NeoForge：21.1.248
- Java：21
- 仅支持服务端

</details>

## English Release Notes

### BREAKING CHANGES

- Project artifacts are now standard Fabric/NeoForge server mods loaded from `mods/`; the legacy launchers remain only as a deprecated fallback.
- Bukkit plugins stay in `plugins/`. Fabric additionally requires Fabric API and `fabric-permissions-api`.

### Added

- Standard Fabric and NeoForge mod distributions with loader entrypoints, metadata, Mixin configurations, and version information.
- Distribution assembly and verification for loader metadata, Mixin configurations, duplicate classes, launcher remnants, and NeoForge jar-in-jar libraries.
- NeoForge jar-in-jar packaging for SnakeYAML, including open-ended dependency metadata and distribution contract tests.
- Native server fixtures and regression coverage for plugin loading, commands, scheduling, events, shutdown, locale resources, and JUL logging.
- Compatibility support for C2ME's chunk system, VanillaBackport's thrown eggs, and LuckPerms permission injection.
- A Paper-style unversioned `CraftSpawnCategory` facade for plugins such as Multiverse-Core.

### Fixed

- Routed Bukkit/JUL logs through Log4j with the server format instead of JUL's default `SimpleFormatter`.
- Fixed the bundled detailed/simple Log4j configurations not being applied, restored ANSI colours and the native console charset, and removed the stray `Ex` from the mistyped throwable converter.
- Restored Fabric console input and command completion, and fixed Jansi classpath conflicts.
- Fixed the duplicated startup banner, wrong version and build time, release-name rendering, ASCII highlighting, and UTF-8 locale output.
- Fixed `org.bukkit.craftbukkit.v.*` remapping (CMILib `CraftLegacy`, CMI `SkullMeta`).
- Fixed Mojang-named reflection for Paper plugins, restoring Multiverse-Core's spawn-category mapping.
- Fixed Fabric Mixin conflicts (anvils, beehives, thrown eggs, C2ME tickets), the `Text.iterator()` join crash, and the rewritten Minecraft version.
- Fixed shutdown hangs, stale embedded JARs, duplicate Fabric bootstrap classes, and plugin class cache reuse across versions.
- Fixed NeoForge SnakeYAML collisions and stale installer artifacts; mapping generation no longer needs a Forge or SRG provider cache.

### Changed

- Updated the project version to `1.0.15`.
- Distribution artifacts and metadata now use the Luminara name and include reproducible Git revision information.
- Shared bootstrap and runtime code moved into `arclight-common`; Fabric and NeoForge provide dedicated entrypoints.
- Build workflows were reorganized with optimized loader builds, distribution validation, and Linux checksums; mappings are derived directly per loader.

**Full Changelog**: https://github.com/CraftAmethyst/Luminara/compare/FK/a1.0.15...FeudalKings/1.0.15
