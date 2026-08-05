# Luminara 1.0.15 Hotfix Release Notes

> Release version: 1.0.15-hotfix
>
> Fixed runtime target: Minecraft 1.20.1, Forge 47.4.22, Java 17.
>
> This is a standalone hotfix release for the 1.0.15 line. It contains the three compatibility fixes listed below.

<details>
<summary>中文发布说明</summary>

# Luminara 1.0.15 Hotfix 发布说明

> 发布版本：1.0.15-hotfix
>
> 固定运行目标：Minecraft 1.20.1、Forge 47.4.22、Java 17。
>
> 本版本是 1.0.15 系列的独立 Hotfix，仅包含以下三个兼容性修复。

## 修复内容

### ModernFix 模组加载死锁

禁用 ModernFix 在 Forge 模组加载阶段对 `ModWorkManager` 工作队列的替换，避免主线程长时间停留在 `ModWorkManagerQueue.pollFirst`，导致加载阶段卡死或表现为死锁。

修订号：4490fa057def483095281f50c25a87a239be8ab5

### YAML i18n 注释注入失效

修复扁平化语言资源键（例如 `locale.comment`、`_v.comment`）无法匹配的问题。

配置节点序列化前重新注入本地化注释；已有但缺少注释的 `luminara.yml` 也会在加载时自动恢复。同步更新各语言资源中的项目仓库和 Issue Tracker 链接。

修订号：867b533a5b5ff24e5eb8cdb27f907a873fda44ff

### FastChunkGen Mixin 冲突

避免 Luminara 的 `DistanceManager` 注入与 FastChunkGen 的同一调用点注入发生冲突。FastChunkGen 存在时，Luminara 会跳过冲突的兼容注入，防止 Mixin 转换失败。

修订号：d7c4dca59ee02bc680bf8ff081297c861b9cdc5b

## 兼容性说明

- 目标 Minecraft：1.20.1
- 目标 Forge：47.4.22
- 目标 Java：17
- 本 Hotfix 不改变 Bukkit API 兼容范围，不新增平台支持。

</details>

## English Release Notes

### ModernFix mod-loading deadlock

Disabled ModernFix's Forge mod-loading replacement for the `ModWorkManager` work queue. This prevents the main thread from remaining in `ModWorkManagerQueue.pollFirst` during mod loading and appearing to deadlock.

Revision: 4490fa057def483095281f50c25a87a239be8ab5

### Broken YAML i18n comment injection

Fixed lookup of flattened locale resource keys such as `locale.comment` and `_v.comment`.

Localized comments are now injected again before configuration nodes are serialized. Existing `luminara.yml` files that are missing comments are repaired during loading. Project repository and Issue Tracker links were also updated in every locale resource.

Revision: 867b533a5b5ff24e5eb8cdb27f907a873fda44ff

### FastChunkGen Mixin conflict

Prevented Luminara's `DistanceManager` injection from colliding with FastChunkGen at the same call site. When FastChunkGen is present, Luminara skips the conflicting compatibility injection instead of failing Mixin transformation.

Revision: d7c4dca59ee02bc680bf8ff081297c861b9cdc5b

## Compatibility

- Target Minecraft: 1.20.1
- Target Forge: 47.4.22
- Target Java: 17
- This hotfix does not change the supported Bukkit API surface or add platform support.
