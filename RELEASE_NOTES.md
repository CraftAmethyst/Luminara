# Luminara 1.0.15 Release Notes

> Scope: commits from `28e4a73faa92f162357a945c9ce64265b4b0516c` through `HEAD`.
>
> Fixed runtime target: Minecraft `1.20.1`, Forge `47.4.22`, Java 17.

<details>
<summary>中文发布说明</summary>

# Luminara 1.0.15 发布说明

> 范围：`28e4a73faa92f162357a945c9ce64265b4b0516c` 至当前最新提交。
>
> 固定运行目标：Minecraft `1.20.1`、Forge `47.4.22`、Java 17。

## 重点更新

- 增加玩家尝试拾取事件支持。`4f3e1a53`
- 暴露运行时兼容性元数据，方便下游工具和插件识别 Minecraft、Forge、Bukkit API 与 Luminara 版本。`efed2f60`, `9ccc53ff`
- 将兼容性 Mixin 从核心配置中隔离，并修复 ModernFix、Radium、Apotheosis 等大型整合包中的兼容问题。`debdbfe0`, `01bf4f2d`, `3d518a03`, `308c807f`, `663f6541`
- 恢复同步世界持久化逻辑，降低异步世界操作造成的数据风险。`375feec2`
- 配置系统迁移至 v2，并完成运行时元数据接入。`81402564`, `9ccc53ff`

## Mixin 与运行时兼容性

- 在实际运行时方法位置注入实体传送事件和作物茎生长事件，保留原有事件语义。`833821b6`, `a3772716`
- 将传送门搜索半径注入固定到运行时调用点，并兼容 Radium 的传送门搜索实现。`3083c3c6`, `3d518a03`
- 将 ejector 相关注入器固定到 Forge 运行时映射。`c49b69ea`
- 在构建期间校验运行时命名的 Mixin 目标，提前发现目标漂移。`c748f98b`
- 将兼容性 Mixin 移至独立配置，避免与核心注入相互污染。`debdbfe0`
- 避免与 ModernFix 配方日志注入冲突。`01bf4f2d`
- 修复 Apotheosis 铁砧注入冲突，并将 portal 共享接口移出 Mixin 专用包，避免类加载器错误。`308c807f`, `663f6541`
- 增加 Mixin 目标清单和访问转换器、映射输入校验。`dc09bdc2`, `2457bf9e`

## 启动器、安装器与运行安全

- 将 Forge 安装器解析逻辑从启动流程中分离。`6e811b71`
- 下载 Forge、Minecraft 和其他运行时工件时增加重试、校验和验证与失败处理。`bd1e51af`
- 验证 Forge bootstrap 指纹，防止加载未预期的启动组件。`7e70f060`
- 使 bootstrap 设置幂等，重复启动不会重复破坏或覆盖状态。`d00a827a`
- EULA 处理改为非交互模式：缺少明确接受状态时报告所需操作，不代替用户写入接受记录。`f55317bc`
- 使用 JDK 原生 Base64 实现 Forge capability 编码。`11ee364b`
- 区分运行时依赖和安装器下载内容，减少错误的依赖复用。`dc01f651`

## 构建与可复现性

- 集中管理 Minecraft、Forge、Java 和其他兼容性版本。`c4c1a271`
- 固定并校验 Gradle Wrapper、插件和依赖版本。`1faafcd6`, `8f2d068f`, `97f12065`, `be95a7c5`
- 集中声明依赖仓库，解析 Forge、Spigot、Mojang 和运行时库时使用明确来源。`9e045b5b`, `b0f6642c`, `ba8befcd`
- 增加统一 Java 构建约定，修复 Spigot 快照和 Mojang 运行时库解析。`cde09080`, `b0f6642c`, `ba8befcd`
- 现代化归档任务，生成确定性分发包和 reobfuscation package mapping。`49bf1029`, `02abb8b4`, `12e218b7`
- 锁定并验证依赖，包括平台相关依赖和传递式 JUnit 模块元数据。`be95a7c5`, `70960b6a`, `28158d1e`
- 支持在本地生成的 Forge 工件参与构建，并在多个任务间复用生成的 Spigot 输入。`50468dcb`, `f15b2ab9`, `523c8d8b`
- 隔离可复现构建工作区，验证 Windows 原生依赖哈希和构建产物兼容性。`2bdd0fab`, `c72e3486`, `4ae8227a`
- 跟踪最新 Forge promotion，并使用实际解析出的 Forge 版本验证分发包。`ab3b6cb5`, `d0a2bdd9`
- 删除旧的发布基础设施；项目不再依赖旧文件服务或遗留发布路径。`5e0d928e`

## CI 与测试

- 将验证工作流合并为统一 CI 流程。`86ad8b1e`
- 保留 Windows 启动器预期失败场景的显式验证。`46e181e6`
- 增加 Forge Mod 与 Bukkit 插件服务器烟测。`c7fd1180`
- 增加分发包兼容性验证。`4ae8227a`
- 验证两次独立构建生成的分发包可复现。`547ae5f3`
- 增加 registry、remapper、Mixin 目标和运行时输入契约测试。`fe8e6590`, `dc09bdc2`, `2457bf9e`

## 文档与项目维护

- 简化 README 的服务器使用说明。`28e4a73f`
- 刷新构建和使用指南。`16dc34bf`
- 增加安全策略与支持指南。`fc1b8006`
- 现代化贡献指南、Issue 模板和项目维护配置。`2951b00a`, `692527e3`
- 增加 Luminara 的 YAML 国际化配置。`7c8e8e3f`, `fb5ae999`
- 统一编辑器设置和仓库换行规则。`7716e8c1`, `da5d7fda`
- 使用 Prettier 统一源文件格式。`6f09ecd7`
- 删除过时的社区元数据。`692527e3`
- 更新 1.0.15 版本元数据、项目版本和发布说明。`e93bbfed`

</details>

## Highlights

- Added the player attempt-pickup event hook. `4f3e1a53`
- Exposed runtime compatibility metadata for downstream tools and plugins. `efed2f60`, `9ccc53ff`
- Isolated compatibility Mixins and fixed compatibility with large modpacks, including ModernFix, Radium, and Apotheosis. `debdbfe0`, `01bf4f2d`, `3d518a03`, `308c807f`, `663f6541`
- Restored synchronous world persistence to reduce the risk of unsafe asynchronous world operations. `375feec2`
- Migrated the configuration system to schema v2 and completed runtime metadata integration. `81402564`, `9ccc53ff`

## Mixin and Runtime Compatibility

- Injected entity-teleport and stem-growth events at the actual runtime methods while preserving event semantics. `833821b6`, `a3772716`
- Targeted portal search radius at the runtime invocation site and coexisted with Radium's portal search implementation. `3083c3c6`, `3d518a03`
- Pinned ejector injectors to Forge runtime mappings. `c49b69ea`
- Validated runtime-named Mixin targets during the build to detect mapping drift early. `c748f98b`
- Isolated compatibility Mixins into a dedicated configuration. `debdbfe0`
- Avoided conflicts with ModernFix recipe-logging hooks. `01bf4f2d`
- Resolved Apotheosis anvil injection conflicts. `308c807f`
- Moved the shared portal interface outside the Mixin-owned package to avoid class-loader failures. `663f6541`
- Added Mixin target inventory and access-transformer/mapping input validation. `dc09bdc2`, `2457bf9e`

## Bootstrap, Installer, and Runtime Safety

- Separated Forge installer parsing from the bootstrap flow. `6e811b71`
- Hardened artifact downloads with retries, checksum validation, and explicit failure handling. `bd1e51af`
- Validated Forge bootstrap fingerprints before loading startup components. `7e70f060`
- Made bootstrap setup idempotent across repeated launches. `d00a827a`
- Made EULA handling noninteractive: missing explicit acceptance is reported to the user instead of being written on the user's behalf. `f55317bc`
- Used JDK Base64 for Forge capability encoding. `11ee364b`
- Separated runtime libraries from installer downloads to avoid incorrect dependency reuse. `dc01f651`

## Build and Reproducibility

- Centralized Minecraft, Forge, Java, and other compatibility versions. `c4c1a271`
- Pinned and verified the Gradle Wrapper, plugins, and dependencies. `1faafcd6`, `8f2d068f`, `97f12065`, `be95a7c5`
- Centralized repository declarations with explicit sources for Forge, Spigot, Mojang, and runtime libraries. `9e045b5b`, `b0f6642c`, `ba8befcd`
- Added shared Java build conventions and fixed Spigot snapshot and Mojang runtime library resolution. `cde09080`, `b0f6642c`, `ba8befcd`
- Modernized archive tasks and added deterministic distribution assembly and reobfuscation package mapping. `49bf1029`, `02abb8b4`, `12e218b7`
- Locked and verified dependencies, including platform-specific entries and transitive JUnit module metadata. `be95a7c5`, `70960b6a`, `28158d1e`
- Reused generated Spigot inputs across build tasks and allowed locally generated Forge artifacts during assembly. `50468dcb`, `f15b2ab9`, `523c8d8b`
- Isolated reproducibility workspaces and verified Windows native dependency hashes. `2bdd0fab`, `c72e3486`, `4ae8227a`
- Tracked the latest Forge promotion and verified distributions against the resolved Forge version. `ab3b6cb5`, `d0a2bdd9`
- Removed legacy publication infrastructure; the project no longer depends on the old file-service or publication paths. `5e0d928e`

## CI and Testing

- Consolidated verification workflows into a single CI pipeline. `86ad8b1e`
- Preserved the expected Windows launcher failure as an explicit CI check. `46e181e6`
- Added Forge mod and Bukkit plugin server smoke tests. `c7fd1180`
- Added distribution compatibility verification. `4ae8227a`
- Verified reproducibility across independent builds. `547ae5f3`
- Added registry, remapper, Mixin-target, and runtime-input contract checks. `fe8e6590`, `dc09bdc2`, `2457bf9e`

## Documentation and Maintenance

- Simplified the README server usage instructions. `28e4a73f`
- Refreshed build and usage guidance. `16dc34bf`
- Added security and support policies. `fc1b8006`
- Modernized contribution guidance, issue templates, and project maintenance configuration. `2951b00a`, `692527e3`
- Added YAML internationalization configuration for Luminara. `7c8e8e3f`, `fb5ae999`
- Standardized editor settings and repository line endings. `7716e8c1`, `da5d7fda`
- Formatted source files consistently with Prettier. `6f09ecd7`
- Removed obsolete community metadata. `692527e3`
- Updated 1.0.15 version metadata, project metadata, and release notes. `e93bbfed`
