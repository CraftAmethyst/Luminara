# Luminara 1.0.15-hotfix

> Minecraft 1.21.1 | Fabric 0.19.3 | NeoForge 21.1.248 | Java 21 | Dedicated Server

<details>
<summary>中文发布说明</summary>

## 中文发布说明

### Fixed

- 修复 NeoForge 上 `CommandNodeHooks` 初始化失败引发的崩溃：`NoSuchFieldException: CURRENT_COMMAND` 与随后的 `NoClassDefFoundError`。玩家加入、数据包函数重载等场景中，FTB Ranks 等权限判断会触发该问题，并连带导致函数加载失败。
  - NeoForge 将 Brigadier 加载在 `MC-BOOTSTRAP` 模块层，Mixin 无法修改 `CommandNode`，因此 `CURRENT_COMMAND` 改为可选字段；缺失时权限检查回退到原版权限等级。
  - 为 NeoForge 的 `CommandHelper.mergeCommandNode` 增加 `CommandNode.canUse` 包装，恢复玩家命令树构建时的 Bukkit 权限判断。
- 新增 `CommandNodeHooks` 回归测试，覆盖字段缺失、命令移除和需求判定。

### Changed

- 版本号更新为 `1.0.15-hotfix`。

</details>

## English Release Notes

### Fixed

- Fixed the NeoForge crash where `CommandNodeHooks` failed to initialize with `NoSuchFieldException: CURRENT_COMMAND` and a subsequent `NoClassDefFoundError`. Permission predicates such as FTB Ranks triggered it during player joins and function reloads, which also caused function loading to fail.
  - NeoForge loads Brigadier from the `MC-BOOTSTRAP` module layer, which Mixin cannot transform. `CURRENT_COMMAND` is now treated as an optional field; when it is absent, permission checks fall back to the vanilla permission level.
  - Wrapped `CommandNode.canUse` in NeoForge's `CommandHelper.mergeCommandNode` to restore Bukkit permission checks while the player command tree is built.
- Added regression tests for `CommandNodeHooks` covering the missing field, command removal, and requirement evaluation.

### Changed

- Updated the project version to `1.0.15-hotfix`.

**Full Changelog**: https://github.com/CraftAmethyst/Luminara/compare/FeudalKings/1.0.15...FeudalKings/1.0.15-hotfix
