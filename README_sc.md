<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/banner-zh-dark.png">
    <img src="assets/banner-zh.png" alt="Luminara - 为 Forge 服务端提供 Bukkit、Spigot 和 Paper 插件兼容能力" width="100%">
  </picture>
</p>

<p align="center">
  <strong>简体中文</strong>
  &nbsp;·&nbsp;
  <a href="./README.md">English</a>
</p>

<p align="center">
  <a href="https://github.com/CraftAmethyst/Luminara/releases"><img alt="最新版本" src="https://img.shields.io/github/v/release/CraftAmethyst/Luminara?style=flat&label=release"></a>
  <a href="https://github.com/CraftAmethyst/Luminara/stargazers"><img alt="GitHub Stars" src="https://img.shields.io/github/stars/CraftAmethyst/Luminara?style=flat&label=stars"></a>
  <a href="https://github.com/CraftAmethyst/Luminara/forks"><img alt="GitHub Forks" src="https://img.shields.io/github/forks/CraftAmethyst/Luminara?style=flat&label=forks"></a>
  <a href="https://github.com/CraftAmethyst/Luminara/issues"><img alt="开放 Issue 数" src="https://img.shields.io/github/issues/CraftAmethyst/Luminara?style=flat&label=issues"></a>
  <a href="https://github.com/CraftAmethyst/Luminara/commits/stable/Trials"><img alt="最后提交" src="https://img.shields.io/github/last-commit/CraftAmethyst/Luminara/stable/Trials?style=flat&label=last%20commit"></a>
  <a href="LICENSE"><img alt="许可证" src="https://img.shields.io/github/license/CraftAmethyst/Luminara?style=flat&label=license"></a>
</p>

<p align="center">
  <a href="https://lum-dl.rimecraft.top/"><strong>下载 Luminara</strong></a>
</p>

<hr>

Luminara 是一个基于 [Arclight](https://github.com/IzzelAliz/Arclight) 服务端开发的分支项目。

## 相比 Arclight 的优势

- 以标准 Mod 形式提供
- 在 1.20.1 分支上支持 Velocity Modern 转发
- 兼容 Adventure、MiniMessage、签名聊天（signed-chat）以及部分常用 Paper API
- 具备更好的模组与插件兼容性

## 从源码构建

项目采用 [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) 1.20.1 布局，一次 Java 17 构建即可生成共享、Fabric 和 Forge 产物：

```bash
./gradlew build
```

| 模块 | 产物 |
| --- | --- |
| `common` | `common/build/libs/luminara-common-1.20.1-*.jar` |
| `fabric` | `fabric/build/libs/luminara-fabric-1.20.1-*.jar` |
| `forge` | `forge/build/libs/luminara-forge-1.20.1-*.jar` |

两个 loader 产物均为自包含 JAR，可直接放入对应服务端的 `mods/` 目录启动。CI 会将两个 loader JAR 与 `build/libs/SHA256SUMS` 统一放入 `build/libs`，可使用 `sha256sum -c build/libs/SHA256SUMS` 校验下载文件。开发流程见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 支持版本


| 组件 | stable/Trials | stable/FeudalKings |
| --- | --- | --- |
| Minecraft | 1.20.1 | 1.21.1 |
| JDK | 17、21 | 21、25 |
| Forge | ≥ 47.4.0 | 不支持 |
| NeoForge | 不支持 | ≥ 21.1.117 |
| Fabric Loader | ≥ 0.16.9 | ≥ 0.16.0 |
| Fabric API | ≥ 0.92.1+1.20.1 | ≥ 0.115.0 |
| CraftBukkit | `v1_20_R1` | `v1_21_R1` |

## 社区支持

- Discord（国际）：https://discord.gg/xn8KGphcvS
- QQ 群 `929252864`（仅中国）：https://qm.qq.com/q/5S00vXfQpq

## 技术支持

一般情况下，在提出问题前，请阅读本项目的 [文档](https://lum.rimecraft.top)，它会帮助你很多。

如果你在使用过程中遇到 Bug，或有更好的建议，欢迎在 [Issue](https://github.com/CraftAmethyst/Luminara/issues) 中提出。

如涉及安全漏洞，请按 [安全政策](SECURITY.md) 的方式私下报告，切勿在群聊中公开讨论或直接提交公开 Issue。

详见 [支持政策（Support policy）](SUPPORT.md)。

## 项目指南

- [文档](https://lum.rimecraft.top)
- [贡献指南（Contributing）](CONTRIBUTING.md)
- [行为准则（Code of conduct）](CODE_OF_CONDUCT.md)
- [安全政策（Security policy）](SECURITY.md)
- [支持政策（Support policy）](SUPPORT.md)
- [上游 Arclight 项目](https://github.com/IzzelAliz/Arclight)

## License

Luminara 依据 [GNU 通用公共许可证 v3.0](LICENSE) 授权。对于继承的代码，Arclight 的署名要求和上游声明仍然适用。
