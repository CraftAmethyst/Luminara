# Luminara

Luminara 是一个基于 [Arclight](https://github.com/IzzelAliz/Arclight) 的仅限 Forge 的 Bukkit 兼容层。它可以让 Forge 模组和 Bukkit 插件在同一个 Minecraft 服务器上共存运行。

> ~~梦想~~目标：实现更多 Paper API，最大限度地提高与模组和插件的兼容性，并提供更多自定义选项。

## 支持的平台

| 组件           | 支持的版本                                    |
| -------------- | --------------------------------------------- |
| Minecraft      | `1.20.1`                                      |
| Forge          | 适用于 Minecraft `1.20.1` 的最新 Forge 正式版 |
| CraftBukkit 包 | `v1_20_R1`                                    |
| Java           | `17`（64 位）                                 |
| Luminara       | `1.0.15-hotfix`                               |

本仓库不支持其他 Minecraft 版本，也不支持 Fabric、NeoForge 或混合加载器等配置。

## 功能特性

- 在 Forge 上提供兼容 Spigot 的 Bukkit API。
- 同时加载 `mods/` 目录下的 Forge 模组和 `plugins/` 目录下的 Bukkit 插件。
- 兼容 Adventure、MiniMessage、签名聊天（signed-chat）及部分 Paper API。
- 通过内置的 `i18n-config` 模块实现本地化配置。
- 默认使用同步、与原版一致的存档持久化方式。
- 在整合包 [ATM9](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9) 和 [落幕曲](https://www.mcmod.cn/modpack/1133.html) 中运行完美。

## 已知不兼容的模组

- [ServerCore](https://modrinth.com/mod/servercore)

  > 它的大部分优化方法源自 Spigot 或 Paper。本服务端本身就是一个 Forge + Spigot + Paper API 服务器，强行加入会导致行为异常或崩溃，因此永远不会兼容

- [Sinytra Connector](https://modrinth.com/mod/connector)

  > 恩情课文：Connector 爷爷使用大量 Fabric API 转译层击落 Forge 服务端兼容性。

- [C2ME Forge](https://www.curseforge.com/minecraft/mc-mods/concurent-chunk-management-engine-forge/d)

  > 没找到源码，所以不修

  > 替代品：[FastChunkGen](https://www.curseforge.com/minecraft/mc-mods/fastchunkgen)

## 已知不兼容的插件

- 任何声称能优化服务器性能的插件（例如 LaggRemover）

  > 这类插件大多是假优化——要么对性能毫无帮助，要么反而会损害性能

- 任何反作弊插件（例如 GrimAC、Matrix、Vulcan）

  > 反作弊插件有时会误判玩家与模组物品的交互（因为这些插件只检测原版行为）~~有些 Bukkit 反作弊其实连原版行为都能多次误判~~

- 70% 使用 Paper API 的插件

  > 本服务器仅支持少数 Paper API——只有十几种常用的——因此你可能会遇到功能缺失、行为异常甚至崩溃的情况。我们目前正在努力实现更多的 Paper API

  > 有些插件提供备选的"Spigot API"版本——那才是最佳的兼容路径

## 如何在此服务端上实现最大优化？

- 如果你想要更激进性能优化，我们推荐我们的下游项目 [PRTS-SERVER](https://github.com/ElainAwa/PRTS-SERVER)

  > PRTS-SERVER 的目标是在尽可能保持兼容性的同时实现更多性能优化补丁。这些补丁来自 [ServerCore](https://modrinth.com/mod/servercore)、[Very Many Players](https://modrinth.com/mod/vmp-forge)、[Paper](https://papermc.io/)、[Mohist](https://www.mohistmc.com/) 等项目

  > 请注意，它只适合中小型整合包！对于像 ATM9 这样的大型整合包，我们建议继续使用本服务端，因为大型整合包往往需要更高的兼容性

  > ~~我最近才发现这破服务端还有下游~~

- 如果你想优化大型整合包，我们推荐一些常见的优化模组组合（下面提到的所有模组都与本服务端兼容）

  > 对大多数整合包来说，仅三个模组就能击落 MSPT：[ModernFix](https://modrinth.com/mod/modernfix)、[FerriteCore](https://modrinth.com/mod/ferrite-core) 和 [Radium](https://modrinth.com/mod/radium)

  > 对于涉及大量探索的整合包，两个模组可以大幅加快区块生成速度：[FastNoise](https://modrinth.com/mod/zfastnoise) 和 [FastChunkGen](https://www.curseforge.com/minecraft/mc-mods/fastchunkgen)

## 从源码构建

使用 64 位 JDK 17 和仓库中自带的 Gradle wrapper。空缓存构建需要能够访问依赖仓库以及 Mojang、Forge 和 Spigot 的构建服务。
构建过程会在配置阶段解析适用于 Minecraft `1.20.1` 的最新 Forge 正式版。使用 `-PforgeVersion=<version>` 可以使用特定的 Forge 构建版本，或在之前已解析过某个版本的情况下离线构建。
由于构建会跟踪最新正式版，Forge 发布新版本时，其新工件尚未包含在 Gradle 依赖校验（`gradle/verification-metadata.xml`）中，构建会在配置阶段失败。此时重新生成校验元数据即可：

```bash
./gradlew :arclight-common:tasks --write-verification-metadata sha256
```

```bash
./gradlew check
./gradlew assembleDistribution
./gradlew verifyDistribution
```

构建是单次通过的；不要运行两次，也不要使用 `--refresh-dependencies` 作为变通方案。构建产物包括：

- `build/distributions/luminara-1.20.1-1.0.15-hotfix.jar`
- `build/distributions/luminara-1.20.1-1.0.15-hotfix.jar.sha256`

使用以下命令校验 SHA256：

```bash
(cd build/distributions && sha256sum -c luminara-1.20.1-1.0.15-hotfix.jar.sha256)
```

使用以下命令运行端到端的 Forge 模组与 Bukkit 插件冒烟测试：

```bash
./gradlew smokeServer
```

在 Windows 上请使用 `gradlew.bat` 而不是 `./gradlew`。~~其实你在 CMD 用 ./gradlew 也可以运行~~

## 运行服务器

1. 将 `luminara-1.20.1-1.0.15-hotfix.jar` 放入一个新的服务器目录中。
2. 阅读 [Minecraft 最终用户许可协议（EULA）](https://aka.ms/MinecraftEULA)。如果你接受该协议，请在该目录中创建 `eula.txt`，内容必须恰好为 `eula=true`。
3. 在该目录中启动 Luminara：

```bash
java -jar luminara-1.20.1-1.0.15-hotfix.jar nogui
```

当 `eula.txt` 缺失或内容不恰好为已接受的值时，启动器会不提示直接退出。它绝不会代你接受 EULA。

将 Forge `1.20.1` 模组放入 `mods/`，将兼容 `v1_20_R1` 的 Bukkit 插件放入 `plugins/`。更改任一组之前请先备份服务器。

## 项目指南

- [贡献指南（Contributing）](CONTRIBUTING.md)
- [行为准则（Code of conduct）](CODE_OF_CONDUCT.md)
- [安全政策（Security policy）](SECURITY.md)
- [支持政策（Support policy）](SUPPORT.md)
- [上游 Arclight 项目](https://github.com/IzzelAliz/Arclight)

## 许可证

Luminara 依据 [GNU 通用公共许可证 v3.0](LICENSE) 授权。对于继承的代码，Arclight 的署名要求和上游声明仍然适用。

~~翻译完成。如果需要，我也可以把这版翻译写入文件（例如 `README_sc.md`）方便直接使用，需要的话告诉我。~~
