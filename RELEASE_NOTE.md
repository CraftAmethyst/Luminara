# Luminara 1.0.15-beta.1 - Feudal Kings

> Minecraft 1.21.1 | NeoForge 21.1.248 | Fabric 0.19.3
>
> 34f79558 ~ c3d56f9a

# BREAKING CHANGES

- The primary distribution is now a standard server mod loaded from `mods/` by Fabric or NeoForge. The legacy launcher JARs remain available only as a deprecated fallback.
- Install the matching dedicated server first. Bukkit plugins remain in `plugins/`, while Fabric additionally requires Fabric API and `fabric-permissions-api`.

# Added

- Standard Fabric and NeoForge mod distributions with loader-specific entrypoints, metadata, Mixin configurations, and version information.
- Distribution assembly and verification for loader metadata, Mixin configurations, duplicate classes, forbidden launcher remnants, and NeoForge jar-in-jar libraries.
- NeoForge jar-in-jar packaging for SnakeYAML, including open-ended dependency metadata and distribution contract tests.
- Native server fixtures and regression coverage for plugin loading, commands, scheduling, events, shutdown, locale resources, and JUL logging.
- Fabric and NeoForge compatibility support for C2ME's rewritten chunk system, VanillaBackport's thrown eggs, and LuckPerms Bukkit permission injection.

# Fixed

- Bukkit and Spigot logs written through `java.util.logging` now use the server Log4j format instead of JUL's default `SimpleFormatter` output on Fabric and NeoForge.
- UTF-8 localized output now uses the runtime console charset consistently; Chinese and other i18n messages no longer become garbled.
- Fabric Mixin conflicts involving anvils, beehives, thrown eggs, and C2ME chunk ticket tracking.
- Fabric player-join crashes caused by the unavailable `net.minecraft.text.Text.iterator()` method.
- Fabric Jansi classpath conflicts and duplicate Fabric bootstrap classes in published artifacts.
- Fabric's exposed Minecraft version being rewritten incorrectly by Fabric Loader.
- Embedded runtime JARs not being refreshed after their contents changed.
- Server shutdown hanging when third-party threads prevent a clean exit; the server now force-exits after the grace period.
- NeoForge startup failures caused by SnakeYAML package collisions and stale installer artifacts.
- `Arclight version null` in startup output and `/version` on the standalone mod path.
- Remapped plugin classes being reused across Luminara versions through stale cache entries.
- Release-name rendering and missing Feudal Kings translations in localized startup output.
- Spigot mapping generation requiring a local Forge or SRG provider cache.

# Changed

- Distribution artifacts and release metadata now use the Luminara name and include reproducible Git revision information.
- Console logging now provides consistent simple and detailed formats, with improved logger filtering and release-name display.
- Shared bootstrap, runtime support, and compatibility code moved into `arclight-common`; Fabric and NeoForge provide dedicated mod entrypoints.
- Fabric and NeoForge build workflows were reorganized, with optimized loader builds, distribution validation, and Linux checksums.
- Mapping generation now derives Fabric/NeoForge mappings directly and skips identity-remap inheritance where appropriate.
- The mapping and installer pipelines no longer depend on the previous Forge cache or SRG provider layout.

<details>

# Luminara 1.0.15-beta.1 - Feudal Kings

> Minecraft 1.21.1 | NeoForge 21.1.248 | Fabric 0.19.3
>
> 34f79558 ~ c3d56f9a

# 破坏性变更

- 主要发行版现在是一个标准服务器模组，由 Fabric 或 NeoForge 从 `mods/` 加载。旧版启动器 JAR 仅作为已弃用的回退方案提供。
- 请先安装匹配的专用服务器。Bukkit 插件仍位于 `plugins/`，而 Fabric 额外需要 Fabric API 和 `fabric-permissions-api`。

# 新增

- 带有加载器特定入口点、元数据、Mixin 配置和版本信息的标准 Fabric 和 NeoForge 模组分发版本。
- 针对加载器元数据、Mixin 配置、重复类、禁止的启动器残留及 NeoForge jar-in-jar 库的分发装配与验证。
- NeoForge 对 SnakeYAML 的 jar-in-jar 打包，包括开放式依赖元数据和分发契约测试。
- 原生服务器夹具及针对插件加载、命令、调度、事件、关闭、区域设置资源和 JUL 日志的回归覆盖。
- 对 C2ME 重写的区块系统、VanillaBackport 的投掷鸡蛋以及 LuckPerms Bukkit 权限注入的 Fabric 和 NeoForge 兼容性支持。

# 修复

- 在 Fabric 和 NeoForge 上，通过 `java.util.logging` 写入的 Bukkit 和 Spigot 日志现在使用服务器 Log4j 格式，而不是 JUL 默认的 `SimpleFormatter` 输出。
- UTF-8 本地化输出现在始终使用运行时控制台字符集；中文及其他 i18n 消息不再乱码。
- 涉及铁砧、蜂巢、投掷鸡蛋和 C2ME 区块 ticket 跟踪的 Fabric Mixin 冲突。
- 由不可用的 `net.minecraft.text.Text.iterator()` 方法导致的 Fabric 玩家加入崩溃。
- Fabric Jansi 类路径冲突以及已发布制品中重复的 Fabric 引导类。
- Fabric Loader 错误重写了 Fabric 暴露的 Minecraft 版本。
- 嵌入式运行时 JAR 在内容更改后未刷新。
- 当第三方线程阻止干净退出时服务器关闭挂起；现在服务器在宽限期后强制退出。
- 由 SnakeYAML 包冲突和过期的安装程序制品导致的 NeoForge 启动失败。
- 独立模组路径中启动输出和 `/version` 里的 `Arclight version null`。
- 重映射插件类因过期缓存条目而在 Luminara 各版本间被复用。
- 本地化启动输出中的发布名称渲染和缺失的 Feudal Kings 翻译。
- Spigot 映射生成需要本地 Forge 或 SRG provider 缓存。

# 变更

- 分发制品和发布元数据现在使用 Luminara 名称，并包含可重现的 Git 修订信息。
- 控制台日志现在提供一致的简单和详细格式，并改进了记录器过滤和发布名称显示。
- 共享引导、运行时支持和兼容性代码已移入 `arclight-common`；Fabric 和 NeoForge 提供专用的模组入口点。
- Fabric 和 NeoForge 构建流程已重新组织，包含优化的加载器构建、分发验证和 Linux 校验和。
- 映射生成现在直接派生 Fabric/NeoForge 映射，并在适当情况下跳过恒等重映射继承。
- 映射和安装程序管道不再依赖先前的 Forge 缓存或 SRG provider 布局。

</details>
