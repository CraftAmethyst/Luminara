# Luminara 2.0.0-alpha.2

> Minecraft 1.20.1 | Forge 47.4.0 | Fabric Loader 0.16.9 | Java 17 | Dedicated Server

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### 破坏性变更

- 项目构建结构迁移到 [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) 的 `common` / `fabric` / `forge` 布局。
- 不再提供单一 Forge 混合端 JAR。现在分别发布：
  - `luminara-forge-1.20.1-2.0.0-alpha.2.jar`
  - `luminara-fabric-1.20.1-2.0.0-alpha.2.jar`
- 内部包名从 `io.izzel.arclight.common.*` 迁移到 `org.craftamethyst.luminara.common.*`.

### 新增

- 新增实验性的 Fabric 支持。
- 新增平台抽象层 `ArclightCommon`.
- 保留 `io.izzel.arclight.common.mixin.core.world.entity.monster.CreeperMixin` 兼容标记类，供 Supplementaries/Moonlight 等按类名探测 Arclight 的模组识别。
- Mixin 中的模组检测在 Forge `ModList` 发布前也可用（回退到 `LoadingModList`）。
- 运行时映射资源随产物打包：`bukkit_srg.srg`、`inheritanceMap.txt`、`spigot-1.20.1-mapped-deobf.jar`。
- 插件类缓存的失效键加入映射文件 SHA-256 指纹，映射更新后不会复用旧的 remap 结果。

### 构建与产物

- 统一构建入口为 `./gradlew build`。
- 移除旧的 `assembleForgeMod`、`assembleFabricMod`、发行物校验和 Forge 冒烟测试等自定义 Gradle 任务。

</details>

## English Release Notes

### Breaking Changes

- The project build structure has been migrated to the `common` / `fabric` / `forge` layout of [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).
- A single Forge hybrid JAR is no longer provided. The following are now released separately:
    - `luminara-forge-1.20.1-2.0.0-alpha.2.jar`
    - `luminara-fabric-1.20.1-2.0.0-alpha.2.jar`
- Internal package names have been migrated from `io.izzel.arclight.common.*` to `org.craftamethyst.luminara.common.*`.

### Added

- Added experimental Fabric support.
- Added the platform abstraction layer `ArclightCommon`.
- Retained the `io.izzel.arclight.common.mixin.core.world.entity.monster.CreeperMixin` compatibility marker class, so mods such as Supplementaries/Moonlight that detect Arclight by class name can still recognize it.
- Mod detection in Mixins now also works before Forge `ModList` is published (falls back to `LoadingModList`).
- Runtime mapping resources are packaged with the artifacts: `bukkit_srg.srg`, `inheritanceMap.txt`, `spigot-1.20.1-mapped-deobf.jar`.
- The invalidation key for the plugin class cache now includes SHA-256 fingerprints of the mapping files, so stale remap results are not reused after mappings are updated.

### Build and Artifacts

- The unified build entry point is `./gradlew build`.
- Removed old custom Gradle tasks such as `assembleForgeMod`, `assembleFabricMod`, release artifact checksums, and Forge smoke tests.
