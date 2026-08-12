# Luminara 1.0.15-beta.1 - Feudal Kings

> Minecraft 1.21.1 | Java 21 | Fabric 0.19.3 and NeoForge 21.1.248

## Highlights

- Fixed several Fabric startup failures caused by Mixin conflicts around anvils, beehives, thrown eggs, and chunk ticket tracking.
- Added compatibility for C2ME's rewritten chunk system and VanillaBackport's thrown-egg behavior.
- Fixed LuckPerms Bukkit permission injection, which previously denied players during login on Luminara.
- Fixed a server crash when a player joined and CraftBukkit attempted to call the missing Fabric `Text.iterator()` method.
- Improved embedded library extraction so updated internal JARs replace stale cached copies after an upgrade.
- Prevented Fabric installer classpath conflicts involving Jansi and cleaned up temporary NeoForge installer artifacts.
- Improved shutdown behavior: if third-party threads prevent a clean exit, the server now terminates after a five-second grace period.
- Modernized console logging, including consistent simple and detailed formats and corrected release-name display.

## Fabric Compatibility Fixes

The following previously reported startup and runtime failures are addressed:

- Arclight anvil maximum-repair-cost injection failures.
- Beehive night-check redirect failures.
- VanillaBackport `ThrownEggMixin` injection failures.
- C2ME chunk ticket manager conflicts.
- Incorrect rewriting of the raw Minecraft version exposed through Fabric Loader.
- Player-join crashes caused by `net.minecraft.text.Text.iterator()` being unavailable.

These are compatibility fixes for the affected code paths, not a guarantee that every combination or version of these mods is supported.

## Bukkit Plugin Compatibility

LuckPerms Bukkit 5.5.x can now initialize its permissible injector on Luminara without treating optional Glowstone classes as required. This resolves login denial caused by `PermissibleInjector` initialization failures.

Component traversal used by CraftBukkit chat conversion now goes through Luminara's compatibility bridge. Join, quit, kick, and similar messages no longer depend on a method that is absent from Fabric's `Text` interface.

## Installation And Upgrade

1. Stop the server completely and back up the world, plugins, mods, and configuration.
2. Replace the previous Luminara launcher JAR with the matching Fabric or NeoForge `1.0.15-beta.1` distribution.
3. Keep using Java 21.
4. Start the server normally with `java -jar <luminara-jar> nogui`.
5. Check the first startup log for failed Mixins or incompatible mod/plugin versions before opening the server to players.

The bootstrap now compares embedded JAR contents during extraction. Manual deletion of `.arclight/mod_file` should no longer be necessary when upgrading between Luminara builds.

## Distribution And Build Changes

- Distribution artifacts are published under the Luminara name for both Fabric and NeoForge.
- Release JARs now undergo structural validation for loader metadata, embedded components, launcher services, Java version, and Minecraft version.
- Build outputs are reproducible and include Git revision metadata.
- Linux CI artifacts include checksums.
- The mapping pipeline no longer depends on a local Forge cache when generating Spigot/SRG mappings.

<details>
<summary>简体中文发布说明</summary>

# Luminara 1.0.15-beta.1 - 诸王

> Minecraft 1.21.1 | Java 21 | Fabric 0.19.3 与 NeoForge 21.1.248

## 主要变化

- 修复铁砧、蜂箱、鸡蛋实体及区块票据追踪相关 Mixin 冲突导致的多项 Fabric 启动失败。
- 增加对 C2ME 重写区块系统及 VanillaBackport 鸡蛋行为的兼容。
- 修复 LuckPerms Bukkit 权限注入失败以及由此导致的玩家登录被拒绝。
- 修复玩家加入时 CraftBukkit 调用 Fabric 中不存在的 `Text.iterator()` 而导致服务器崩溃的问题。
- 改进内嵌依赖提取逻辑，升级后会正确替换内容已经变化的缓存 JAR。
- 避免 Fabric 安装器中的 Jansi 类路径冲突，并清理 NeoForge 安装器生成的临时文件。
- 改进服务器关闭流程：如果第三方线程阻止正常退出，服务器会在五秒宽限期后强制结束进程。
- 更新控制台日志配置，统一简洁与详细格式，并修正发布代号显示。

## Fabric 兼容性修复

本版本处理了以下已报告的启动或运行错误：

- Arclight 铁砧最大修复费用注入失败。
- 蜂箱夜间检查重定向失败。
- VanillaBackport `ThrownEggMixin` 注入失败。
- C2ME 区块票据管理器冲突。
- Fabric Loader 中原始 Minecraft 版本被错误改写。
- 玩家加入时因 `net.minecraft.text.Text.iterator()` 不存在而发生的崩溃。

这些修改针对已知冲突路径提供兼容，并不代表所有相关模组版本或任意组合都受到保证支持。

## Bukkit 插件兼容性

LuckPerms Bukkit 5.5.x 现在可以在 Luminara 上正确初始化权限注入器，不再将可选的 Glowstone 类误判为必需依赖。这解决了 `PermissibleInjector` 初始化失败造成的登录拒绝。

CraftBukkit 聊天文本转换现在通过 Luminara 的兼容桥遍历文本组件。加入、退出、踢出等消息不再依赖 Fabric `Text` 接口中不存在的方法。

## 安装与升级

1. 完全关闭服务器，并备份世界、插件、模组和配置文件。
2. 使用对应平台的 `1.0.15-beta.1` Fabric 或 NeoForge 发行包替换旧 Luminara 启动 JAR。
3. 继续使用 Java 21。
4. 使用 `java -jar <luminara-jar> nogui` 正常启动服务器。
5. 对玩家开放服务器前，检查首次启动日志中是否仍有 Mixin 失败或模组、插件版本不兼容提示。

启动器现在会在提取时比较内嵌 JAR 的实际内容。升级 Luminara 构建后，通常不再需要手动删除 `.arclight/mod_file`。

## 发行与构建变化

- Fabric 和 NeoForge 发行文件均统一使用 Luminara 名称。
- 发行 JAR 会验证加载器元数据、内嵌组件、启动服务、Java 版本和 Minecraft 版本。
- 构建结果可复现，并包含 Git 修订版本信息。
- Linux CI 产物附带校验和。
- 生成 Spigot/SRG 映射时不再依赖本地 Forge 缓存。

</details>
