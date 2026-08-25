# Luminara 1.0.15-beta.1 - Feudal Kings

> Minecraft 1.21.1 | Java 21 | Fabric 0.19.3 and NeoForge 21.1.248
>
> Covers every change since 1.0.15-Alpha.

## Highlights

- Luminara is now a standard server mod: drop the JAR into `mods/` on an ordinary Fabric or NeoForge dedicated server. The launcher JARs remain only as a deprecated fallback.
- Fixed a NeoForge boot crash triggered by any other mod that bundles SnakeYAML (`java.lang.module.ResolutionException: Modules org.yaml.snakeyaml and arclight export package ...`).
- Bukkit log output now uses the server log format instead of the raw `java.util.logging` format.
- Bukkit no longer reports `Arclight version null` on startup and in `/version`.
- Remapped plugin classes are no longer reused across Luminara versions, so upgrades cannot resurrect stale cached bytecode.
- Fixed several Fabric startup failures caused by Mixin conflicts around anvils, beehives, thrown eggs, and chunk ticket tracking.
- Added compatibility for C2ME's rewritten chunk system and VanillaBackport's thrown-egg behavior.
- Fixed LuckPerms Bukkit permission injection, which previously denied players during login on Luminara.
- Fixed a server crash when a player joined and CraftBukkit attempted to call the missing Fabric `Text.iterator()` method.
- Improved shutdown behavior: if third-party threads prevent a clean exit, the server now terminates after a five-second grace period.
- Modernized console logging, including consistent simple and detailed formats and corrected release-name display.

## Standard Server Mod Distribution

The primary install artifact is a plain mod JAR per loader, loaded from `mods/` by the loader itself:

- Shared bootstrap and runtime support moved into `arclight-common`, with loader-specific entrypoints, Mixin configurations, and metadata.
- Each release JAR is verified for loader metadata, Mixin configurations, version metadata, absence of launcher remnants, and duplicate classes.
- The legacy launcher JARs are still built as a transitional fallback and regression baseline. They are deprecated and are not the recommended installation path.
- Native server fixtures and regression scripts cover plugin loading, commands, scheduling, and graceful shutdown.

## Standalone Mod Fixes

Behavior the old launch chain used to provide is now provided by the mod itself:

- **SnakeYAML no longer collides with other mods.** A mod JAR is exposed to the Java module system as an automatic module that exports every package it contains, so the merged copy of SnakeYAML clashed with the copy other mods ship through jar-in-jar and the game layer failed to resolve. SnakeYAML now ships as a jar-in-jar entry, and NeoForge keeps a single copy per `group:artifact`.
- **Bukkit logging reaches Log4j again.** Bukkit logs through `java.util.logging`; without the launcher's transformer those records went to the default JUL console handler and were printed in `SimpleFormatter` form. A bridge now forwards them to Log4j with the original logger name and mapped level.
- **The server version is reported again.** `CraftServer#getVersion` reads the `arclight.version` property, which the launcher used to publish. The mod publishes it from its own build metadata.
- **The plugin class cache is keyed by version.** The cache key used a manifest implementation version that is invisible under the mod class loader, so `.arclight/class_cache` survived upgrades. The first start after upgrading now rebuilds it.

## Fabric Compatibility Fixes

The following previously reported startup and runtime failures are addressed:

- Arclight anvil maximum-repair-cost injection failures.
- Beehive night-check redirect failures.
- VanillaBackport `ThrownEggMixin` injection failures.
- C2ME chunk ticket manager conflicts.
- Incorrect rewriting of the raw Minecraft version exposed through Fabric Loader.
- Player-join crashes caused by `net.minecraft.text.Text.iterator()` being unavailable.
- Jansi classpath conflicts during Fabric installation, and duplicate Fabric bootstrap classes in published artifacts.

These are compatibility fixes for the affected code paths, not a guarantee that every combination or version of these mods is supported.

## Bukkit Plugin Compatibility

LuckPerms Bukkit 5.5.x can now initialize its permissible injector on Luminara without treating optional Glowstone classes as required. This resolves login denial caused by `PermissibleInjector` initialization failures.

Component traversal used by CraftBukkit chat conversion now goes through Luminara's compatibility bridge. Join, quit, kick, and similar messages no longer depend on a method that is absent from Fabric's `Text` interface.

Plugin log records, including those written through `java.util.logging`, now appear in the server log format.

## Installation And Upgrade

1. Stop the server completely and back up the world, plugins, mods, and configuration.
2. Install the matching dedicated server: Fabric 0.19.3 or NeoForge 21.1.248 for Minecraft 1.21.1.
3. Put `luminara-neoforge-1.21.1-1.0.15-beta.1.jar` in `mods/` on NeoForge, or `luminara-fabric-1.21.1-1.0.15-beta.1.jar` in `mods/` on Fabric.
4. On Fabric, also install Fabric API and `fabric-permissions-api` in `mods/`.
5. Keep Bukkit plugins in `plugins/` and keep using Java 21.
6. Start the loader's normal dedicated-server command with `nogui`.
7. Check the first startup log for failed Mixins or incompatible mod/plugin versions before opening the server to players.

Coming from a launcher-based install: remove the old Luminara launcher JAR and start the loader's server instead. Worlds, plugins, and configuration are unchanged. The first start after upgrading rebuilds the remapped plugin class cache, which takes slightly longer.

## Distribution And Build Changes

- Distribution artifacts are published under the Luminara name for both Fabric and NeoForge.
- Third-party libraries that must keep their original package names ship as jar-in-jar entries; the build fails if a package is both merged into the mod JAR and nested, or if a nested library declares a version instead of an open-ended range.
- Build outputs are reproducible and include Git revision metadata.
- Linux CI artifacts include checksums.
- The mapping pipeline no longer depends on a local Forge cache when generating Spigot/SRG mappings.

<details>
<summary>简体中文发布说明</summary>

# Luminara 1.0.15-beta.1 - 诸王

> Minecraft 1.21.1 | Java 21 | Fabric 0.19.3 与 NeoForge 21.1.248
>
> 涵盖 1.0.15-Alpha 之后的全部改动。

## 主要变化

- Luminara 现在是标准服务端模组：把 JAR 放进普通 Fabric 或 NeoForge 服务端的 `mods/` 即可。启动器 JAR 仅作为已弃用的回退方案保留。
- 修复只要其它模组内置 SnakeYAML 就会导致 NeoForge 启动崩溃的问题（`java.lang.module.ResolutionException: Modules org.yaml.snakeyaml and arclight export package ...`）。
- Bukkit 日志改用服务端日志格式输出，不再使用 `java.util.logging` 的原始格式。
- 启动日志与 `/version` 不再显示 `Arclight version null`。
- 重映射后的插件字节码不再跨 Luminara 版本复用，升级后不会再命中过期缓存。
- 修复铁砧、蜂箱、鸡蛋实体及区块票据追踪相关 Mixin 冲突导致的多项 Fabric 启动失败。
- 增加对 C2ME 重写区块系统及 VanillaBackport 鸡蛋行为的兼容。
- 修复 LuckPerms Bukkit 权限注入失败以及由此导致的玩家登录被拒绝。
- 修复玩家加入时 CraftBukkit 调用 Fabric 中不存在的 `Text.iterator()` 而导致服务器崩溃的问题。
- 改进服务器关闭流程：如果第三方线程阻止正常退出，服务器会在五秒宽限期后强制结束进程。
- 更新控制台日志配置，统一简洁与详细格式，并修正发布代号显示。

## 标准服务端模组发行

现在每个加载器的主发行产物都是普通模组 JAR，由加载器自身从 `mods/` 加载：

- 共用的引导与运行时支持迁入 `arclight-common`，各加载器有独立的入口点、Mixin 配置与元数据。
- 每个发行 JAR 都会校验加载器元数据、Mixin 配置、版本元数据、是否残留启动器组件以及是否存在重复类。
- 旧的启动器 JAR 仍会构建，作为过渡回退与回归基线，已弃用，不再是推荐安装方式。
- 新增原生服务端测试夹具与回归脚本，覆盖插件加载、命令、调度与优雅关闭。

## 标准模组化带来的修复

原先由启动链提供的行为，现在由模组自身提供：

- **SnakeYAML 不再与其它模组冲突。** 模组 JAR 在 Java 模块系统中是自动模块，会导出其中的所有包，因此内置的 SnakeYAML 与其它模组通过 jar-in-jar 提供的副本冲突，game 层解析直接失败。现在 SnakeYAML 以 jar-in-jar 方式随包发布，NeoForge 会按 `group:artifact` 只保留一份。
- **Bukkit 日志重新进入 Log4j。** Bukkit 通过 `java.util.logging` 输出日志；缺少启动器的字节码改写后，这些记录落到 JUL 默认控制台处理器，以 `SimpleFormatter` 格式打印。现在由桥接器转发到 Log4j，保留原 logger 名称并映射日志级别。
- **服务端版本重新可见。** `CraftServer#getVersion` 读取 `arclight.version` 属性，该属性原本由启动器写入，现在由模组用自身构建元数据发布。
- **插件类缓存按版本区分。** 缓存键此前依赖 manifest 中的实现版本，而模组类加载器下取不到该值，导致 `.arclight/class_cache` 跨版本存活。升级后首次启动会重建缓存。

## Fabric 兼容性修复

本版本处理了以下已报告的启动或运行错误：

- Arclight 铁砧最大修复费用注入失败。
- 蜂箱夜间检查重定向失败。
- VanillaBackport `ThrownEggMixin` 注入失败。
- C2ME 区块票据管理器冲突。
- Fabric Loader 中原始 Minecraft 版本被错误改写。
- 玩家加入时因 `net.minecraft.text.Text.iterator()` 不存在而发生的崩溃。
- Fabric 安装过程中的 Jansi 类路径冲突，以及发行产物中重复的 Fabric 引导类。

这些修改针对已知冲突路径提供兼容，并不代表所有相关模组版本或任意组合都受到保证支持。

## Bukkit 插件兼容性

LuckPerms Bukkit 5.5.x 现在可以在 Luminara 上正确初始化权限注入器，不再将可选的 Glowstone 类误判为必需依赖。这解决了 `PermissibleInjector` 初始化失败造成的登录拒绝。

CraftBukkit 聊天文本转换现在通过 Luminara 的兼容桥遍历文本组件。加入、退出、踢出等消息不再依赖 Fabric `Text` 接口中不存在的方法。

插件日志（包括通过 `java.util.logging` 输出的部分）现在也使用服务端日志格式。

## 安装与升级

1. 完全关闭服务器，并备份世界、插件、模组和配置文件。
2. 安装对应的服务端：Minecraft 1.21.1 的 Fabric 0.19.3 或 NeoForge 21.1.248。
3. NeoForge 把 `luminara-neoforge-1.21.1-1.0.15-beta.1.jar` 放进 `mods/`；Fabric 把 `luminara-fabric-1.21.1-1.0.15-beta.1.jar` 放进 `mods/`。
4. Fabric 还需要在 `mods/` 中安装 Fabric API 与 `fabric-permissions-api`。
5. Bukkit 插件继续放在 `plugins/`，继续使用 Java 21。
6. 用加载器自身的服务端启动命令加 `nogui` 启动。
7. 对玩家开放服务器前，检查首次启动日志中是否仍有 Mixin 失败或模组、插件版本不兼容提示。

从启动器方式升级：删除旧的 Luminara 启动器 JAR，改用加载器的服务端启动。世界、插件与配置无需改动。升级后首次启动会重建插件类缓存，耗时略长。

## 发行与构建变化

- Fabric 和 NeoForge 发行文件均统一使用 Luminara 名称。
- 必须保留原始包名的第三方库以 jar-in-jar 方式随包发布；若某个包同时被合并进模组 JAR 与嵌套发布，或嵌套库声明的是固定版本而非开区间，构建会直接失败。
- 构建结果可复现，并包含 Git 修订版本信息。
- Linux CI 产物附带校验和。
- 生成 Spigot/SRG 映射时不再依赖本地 Forge 缓存。

</details>
