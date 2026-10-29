# Luminara 2.0.0-alpha.2

> Minecraft 1.20.1 | Forge 47.4.0 | Fabric Loader 0.16.9 | Java 17 | Dedicated Server

> **警告 / Warning:** `2.0.0-alpha.2` 在 Fabric 上启用了有限的 Bukkit Mixin 集合，并通过测试插件加载、命令分派和服务器关闭验证；Forge 的 Bukkit Mixin 注入仍未启用，更多插件兼容性尚未验证。详见下方“已知限制”。`2.0.0-alpha.2` enables a limited Bukkit Mixin set on Fabric, verified with a test plugin, command dispatch, and server shutdown. Forge Bukkit Mixin injection remains disabled, and broader plugin compatibility is unverified. See "Known limitations" below.

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### BREAKING CHANGES

- 项目构建结构迁移到 [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) 的 `common` / `fabric` / `forge` 布局。
- 不再提供单一 Forge 混合端 JAR。Forge 与 Fabric 分别产出 `luminara-forge-1.20.1-*.jar` 和 `luminara-fabric-1.20.1-*.jar`。
- 两个 loader 产物均为自包含 JAR（约 21 MB），已打包运行所需依赖。
- Fabric 已启用有限的 Bukkit/Spigot/Paper Mixin 集合；Forge 注入和更广泛的插件兼容能力仍在迁移中，详见“已知限制”。

### Added

- 新增 Fabric Loader 发行版，包含 `preLaunch` 与 `main` 入口点。
- 新增共享 `common` 模块，Forge 与 Fabric 复用同一套核心代码和 access widener。
- 在 Mixin 应用前完成平台服务引导，避免早期类加载阶段访问未初始化的注册表。
- Forge `runServer` 运行时自动加载打包的 loader 依赖。
- 两个 loader 的构建均输出 sources 与 javadoc JAR。
- CI 在 `./gradlew build` 后将两个 loader JAR 暂存到根目录 `build/libs`，并生成单个 `SHA256SUMS` 一同上传。
- Fabric 启用经过收敛的核心、Bukkit 和 loader 专属 Mixin 配置，并生成非空 refmap。
- Fabric 加入 Bukkit 材料、世界与区块管理桥接，以及插件命令和区块票据桥接。

### Fixed

- 修复 Forge 独立服务端日志被替换为 `uminaraFormatting` 的问题；日志布局不再依赖尚未注册的自定义 Log4j 转换器。
- 修复 Forge 与 Fabric 构建产物在真实专用服务端上的启动问题。
- 修复 Forge 开发环境 `runServer` 找不到打包依赖的问题。
- 修复 Forge 控制台 Ctrl-C 关服钩子未命中 `MinecraftServer.halt` 的问题，恢复正常的服务器线程关服流程。
- 将 Gradle 脚本、`buildSrc` 约定和模块任务对齐 MultiLoader 模板；`./gradlew build` 可直接产出全部构建物。
- 修复 Fabric 专用服务器上的 Bukkit 插件初始化与命令分派，并将 Bukkit 默认配置打包进 Forge/Fabric 产物。

### Changed

- 版本号更新为 `2.0.0-alpha.2`。
- Forge 目标更新为 `1.20.1-47.4.0`；Fabric Loader 固定为 `0.16.9`，Fabric API 为 `0.92.1+1.20.1`。
- Forge 日志改用标准 `%msg{nolookup}` 输出，保留可读的控制台与服务端日志。
- 移除自定义的 `assembleForgeMod`、`assembleFabricMod`、发行物校验和冒烟测试 Gradle 任务，构建入口统一为 `./gradlew build`。

### 已知限制

- Forge 产物当前没有声明 `MixinConfigs`，因此 Forge 上的 Bukkit/Spigot/Paper 插件注入尚未生效。
- Fabric 只启用了经过收敛的 Mixin 子集；完整 Bukkit、Spigot、Paper API、第三方插件和模组兼容性尚未回归验证。
- Fabric 专用服务器已验证启动、世界创建、测试插件加载、命令分派和正常关闭；真实插件区块票据使用及更广泛的插件行为仍需测试。
- Forge Mixin 注解处理器与 refmap 仍待恢复；部分共享 Mixin 需要先拆分 Forge 专属实现。

### 兼容性

- Minecraft：1.20.1
- Forge：1.20.1-47.4.0
- Fabric Loader：0.16.9
- Fabric API：0.92.1+1.20.1
- CraftBukkit：`v1_20_R1`
- Java：17
- 环境：仅专用服务端

</details>

## English Release Notes

### BREAKING CHANGES

- The build now follows the [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) `common` / `fabric` / `forge` layout.
- The single Forge hybrid-server JAR is no longer produced. Forge and Fabric now ship as `luminara-forge-1.20.1-*.jar` and `luminara-fabric-1.20.1-*.jar` respectively.
- Both loader artifacts are self-contained (about 21 MB) and bundle their runtime dependencies.
- Fabric now enables a limited Bukkit/Spigot/Paper Mixin set. Forge injection and broader plugin compatibility are still being ported; see "Known limitations".

### Added

- Added a Fabric Loader distribution with `preLaunch` and `main` entrypoints.
- Added a shared `common` module so Forge and Fabric reuse the same core code and access widener.
- Bootstrapped platform services before Mixin application to avoid touching uninitialized registries during early class loading.
- Bundled loader dependencies are now placed on the `runServer` classpath automatically.
- Both loaders now produce sources and javadoc JARs.
- CI stages both loader JARs into the root `build/libs` after `./gradlew build` and generates a single `SHA256SUMS` for upload.
- Enabled a reduced set of core, Bukkit, and loader-specific Fabric Mixins with a non-empty refmap.
- Added Fabric bridges for Bukkit materials, world and chunk management, plugin commands, and chunk tickets.

### Fixed

- Fixed Forge dedicated-server logs being replaced by `uminaraFormatting`; log layouts no longer depend on a custom Log4j converter that was not registered yet.
- Fixed startup of the Forge and Fabric artifacts on real dedicated servers.
- Fixed missing bundled dependencies during Forge `runServer` development launches.
- Fixed the Forge console Ctrl-C shutdown hook targeting the wrong `halt` owner, restoring the normal server-thread shutdown path.
- Aligned Gradle scripts, `buildSrc` conventions, and module tasks with the MultiLoader template; `./gradlew build` now produces every build output directly.
- Fixed Bukkit plugin initialization and command dispatch on Fabric dedicated servers, and bundled Bukkit default configuration files in both loader artifacts.

### Changed

- Updated the project version to `2.0.0-alpha.2`.
- Updated the Forge target to `1.20.1-47.4.0`; pinned Fabric Loader to `0.16.9` and Fabric API to `0.92.1+1.20.1`.
- Switched Forge logging to standard `%msg{nolookup}` output for readable console and server logs.
- Removed the custom `assembleForgeMod`, `assembleFabricMod`, distribution verification, and smoke test Gradle tasks. `./gradlew build` is the single build entrypoint.

### Known limitations

- The Forge artifact currently declares no `MixinConfigs`, so Bukkit/Spigot/Paper plugin injection is not active on Forge.
- Fabric enables only a reduced Mixin subset. Full Bukkit, Spigot, Paper API, third-party plugin, and mod compatibility has not been regression-tested.
- Fabric verification covers dedicated-server startup, world creation, a test plugin loading, command dispatch, and clean shutdown. Real plugin chunk-ticket use and broader plugin behavior still need testing.
- Forge Mixin annotation processing and refmap generation still need to be restored; some shared Mixins require Forge-specific splits first.
