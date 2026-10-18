# Luminara 1.0.16

> Minecraft 1.20.1 | Forge 47.4.23 | Java 17 | Dedicated Server

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### BREAKING CHANGES

- 本项目产物现在以标准 Forge Mod JAR 形式提供，不再使用旧的混合端/补丁式分发方式。
- 同时使用 Radium、LostCities 和 FastChunkGen 时，必须保持 FastChunkGen 的 `[threadedWorldGen] enabled = false`。启用异步世界生成会破坏 `WorldGenRegion` 的区块顺序，并导致 `No chunk exists` 崩溃。

### Added

- 实现 `PotionEffectType#getEffectCategory()` 及效果分类颜色。
- 实现 `PlayerAdvancementCriterionGrantEvent`，支持取消并撤销 criterion 进度。
- 实现 `PreCreatureSpawnEvent`，覆盖自然生成、刷怪笼、`EntityType#spawn` 和 `SpawnUtil#trySpawnMob`。
- 为 Multiverse-Core 暴露 `EntityType#getCategory()`、实体分类别名字段及无版本号 `CraftSpawnCategory#toBukkit()`。
- 补充六种语言的 `dist.logic-world-check`。
- 新增关服超时强制退出机制。

### Fixed

- 统一 Bukkit/JUL 日志到 Log4j 输出格式。
- 修复控制台对 `§x...` 和 `&x...` RGB 颜色的渲染。
- 确保日志配置在启动 ASCII 横幅之前应用。
- 修复 FastChunkGen 与 Hyperbox 之间的空 `IOWorker` 路径崩溃。
- 修复 FastChunkGen 与 Luminara `DistanceManager` 的注入冲突。
- 修复 Multiverse 分类映射中 `getCategory`、`CraftSpawnCategory` 和实体别名字段缺失的问题。
- 修复实体别名引起的 `minecraft`/`luminara` 模块 split package 冲突，以及接口过早初始化造成的空字段。
- 恢复异步访问保护，并修复服务器关闭后进程残留。

### Changed

- 版本号更新为 `1.0.16`。
- Luminara 的 Forge 依赖顺序改为在 Forge 和 Minecraft 之前加载。
- 验证运行目标更新为 Forge `47.4.23`。
- 发行物继续使用标准 Forge Mod JAR，并执行依赖校验和发行物校验。

### 兼容性

- Minecraft：1.20.1
- Forge：47.4.x
- Java：17
- 环境：仅服务端

</details>

## English Release Notes

### BREAKING CHANGES

- Project artifacts are now distributed as a standard Forge Mod JAR instead of the legacy hybrid-server/patch-style distribution.
- When Radium, LostCities, and FastChunkGen are used together, FastChunkGen's `[threadedWorldGen] enabled` must remain `false`. Asynchronous world generation breaks `WorldGenRegion` chunk ordering and can cause `No chunk exists` crashes.

### Added

- Implemented `PotionEffectType#getEffectCategory()` and effect category colors.
- Implemented `PlayerAdvancementCriterionGrantEvent` with cancellation and criterion rollback.
- Implemented `PreCreatureSpawnEvent` for natural spawning, spawners, `EntityType#spawn`, and `SpawnUtil#trySpawnMob`.
- Exposed Multiverse-Core aliases for `EntityType#getCategory()`, entity category fields, and unversioned `CraftSpawnCategory#toBukkit()`.
- Added `dist.logic-world-check` translations for all six locales.
- Added a shutdown timeout that forcefully terminates a hung server process.

### Fixed

- Routed Bukkit/JUL logs through Log4j with a consistent format.
- Fixed console rendering for `§x...` and `&x...` RGB colors.
- Applied logging configuration before the startup ASCII banner.
- Fixed the null `IOWorker` path crash between FastChunkGen and Hyperbox.
- Fixed the `DistanceManager` injection conflict with FastChunkGen.
- Fixed missing Multiverse `getCategory`, `CraftSpawnCategory`, and entity alias fields.
- Removed the `minecraft`/`luminara` split-package conflict and fixed aliases being initialized as null.
- Restored asynchronous access guards and fixed lingering server processes after shutdown.

### Changed

- Updated the project version to `1.0.16`.
- Changed Forge dependency ordering so Luminara loads before Forge and Minecraft.
- Updated the validated runtime target to Forge `47.4.23`.
- Continued shipping as a standard Forge Mod JAR with dependency and distribution verification.
