# Luminara 2.0.0-alpha.1

> Minecraft 1.20.1 | Forge 47.4.0 | Fabric Loader 0.16.9 | Java 17 | Dedicated Server

> **警告 / Warning:** 当前 `2.0.0-alpha.1` 的 Forge/Fabric JAR 可以启动，但尚未启用 Bukkit/Spigot/Paper 的 Mixin 注入，Bukkit 插件不会加载。详见下方“已知限制”。The current `2.0.0-alpha.1` Forge/Fabric JARs start, but Bukkit/Spigot/Paper Mixin injection is not enabled; Bukkit plugins will not load. See "Known limitations" below.

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### BREAKING CHANGES

- 项目构建结构迁移到 [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) 的 `common` / `fabric` / `forge` 布局。
- 不再提供单一 Forge 混合端 JAR。Forge 与 Fabric 分别产出 `luminara-forge-1.20.1-*.jar` 和 `luminara-fabric-1.20.1-*.jar`。
- 两个 loader 产物均为自包含 JAR（约 21 MB），已打包运行所需依赖。
- 本 alpha 版本尚未在产物中启用 Bukkit/Spigot/Paper 的 Mixin 注入，插件兼容能力仍在迁移中；详见“已知限制”。

### Added

- 新增 Fabric Loader 发行版，包含 `preLaunch` 与 `main` 入口点。
- 新增共享 `common` 模块，Forge 与 Fabric 复用同一套核心代码和 access widener。
- 在 Mixin 应用前完成平台服务引导，避免早期类加载阶段访问未初始化的注册表。
- Forge `runServer` 运行时自动加载打包的 loader 依赖。
- 两个 loader 的构建均输出 sources 与 javadoc JAR。
- CI 在 `./gradlew build` 后将两个 loader JAR 暂存到根目录 `build/libs`，并生成单个 `SHA256SUMS` 一同上传。

### Fixed

- 修复 Forge 独立服务端日志被替换为 `uminaraFormatting` 的问题；日志布局不再依赖尚未注册的自定义 Log4j 转换器。
- 修复 Forge 与 Fabric 构建产物在真实专用服务端上的启动问题。
- 修复 Forge 开发环境 `runServer` 找不到打包依赖的问题。
- 将 Gradle 脚本、`buildSrc` 约定和模块任务对齐 MultiLoader 模板；`./gradlew build` 可直接产出全部构建物。

### Changed

- 版本号更新为 `2.0.0-alpha.1`。
- Forge 目标更新为 `1.20.1-47.4.0`；Fabric Loader 固定为 `0.16.9`，Fabric API 为 `0.92.1+1.20.1`。
- Forge 日志改用标准 `%msg{nolookup}` 输出，保留可读的控制台与服务端日志。
- 移除自定义的 `assembleForgeMod`、`assembleFabricMod`、发行物校验和冒烟测试 Gradle 任务，构建入口统一为 `./gradlew build`。

### 已知限制

- Forge 产物当前没有声明 `MixinConfigs`，Fabric `fabric.mod.json` 的 `mixins` 为空；Bukkit/Spigot/Paper 兼容注入在这些 alpha 产物中尚未生效。
- Forge Mixin 注解处理器与 refmap 仍待恢复；部分共享 Mixin 需要先拆分 Forge 专属实现。
- 当前验证范围为专用服务端启动、世界创建与正常关闭；插件与模组兼容性回归尚未覆盖。

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
- This alpha does not yet enable Bukkit/Spigot/Paper Mixin injection in the produced artifacts. Plugin compatibility is still being ported; see "Known limitations".

### Added

- Added a Fabric Loader distribution with `preLaunch` and `main` entrypoints.
- Added a shared `common` module so Forge and Fabric reuse the same core code and access widener.
- Bootstrapped platform services before Mixin application to avoid touching uninitialized registries during early class loading.
- Bundled loader dependencies are now placed on the `runServer` classpath automatically.
- Both loaders now produce sources and javadoc JARs.
- CI stages both loader JARs into the root `build/libs` after `./gradlew build` and generates a single `SHA256SUMS` for upload.

### Fixed

- Fixed Forge dedicated-server logs being replaced by `uminaraFormatting`; log layouts no longer depend on a custom Log4j converter that was not registered yet.
- Fixed startup of the Forge and Fabric artifacts on real dedicated servers.
- Fixed missing bundled dependencies during Forge `runServer` development launches.
- Aligned Gradle scripts, `buildSrc` conventions, and module tasks with the MultiLoader template; `./gradlew build` now produces every build output directly.

### Changed

- Updated the project version to `2.0.0-alpha.1`.
- Updated the Forge target to `1.20.1-47.4.0`; pinned Fabric Loader to `0.16.9` and Fabric API to `0.92.1+1.20.1`.
- Switched Forge logging to standard `%msg{nolookup}` output for readable console and server logs.
- Removed the custom `assembleForgeMod`, `assembleFabricMod`, distribution verification, and smoke test Gradle tasks. `./gradlew build` is the single build entrypoint.

### Known limitations

- The Forge artifact currently declares no `MixinConfigs`, and Fabric's `fabric.mod.json` has an empty `mixins` list. Bukkit/Spigot/Paper compatibility injection is not active in these alpha artifacts.
- Forge Mixin annotation processing and refmap generation still need to be restored; some shared Mixins require Forge-specific splits first.
- Verification currently covers dedicated-server startup, world creation, and clean shutdown. Plugin and mod compatibility regressions are not yet covered.
