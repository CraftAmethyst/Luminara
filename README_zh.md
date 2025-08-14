# Luminara

[English](/README.md)

> 本服务端是一个 Arclight Fork，所以使用本服务端过程中出现的任何问题请在本项目 Issue 反馈，请勿在 Arclight 项目 Issue 反馈！

## ❓ 这啥玩意

Luminara 是一个混合服务端，在 Forge 的基础上通过 Mixin 实现 Bukkit/Spigot/Paper API（Forge+Paper），就像 Mohist/Thermos/MCPC+那样。

## ✨ 特性

- 🔧 **兼容性强** - 支持 Bukkit/Spigot/部分 Paper 插件与 Forge 模组同时运行
- 🚀 **高性能** - 合并来自 Paper 的优化
- 🛠️ **易于使用** - 简单的安装与使用
- 🌐 **Velocity 支持** - 支持 Velocity Modern 转发，实现跨服功能

## 🎯 主要维护版本

> **当前主要维护版本：Minecraft 1.20.1**
>
> - **Forge 版本**：47.4.6
> - **插件兼容性**：一般，支持 Bukkit/Spigot 与部分 Paper 插件
> - **模组兼容性**：较好

## 📥 下载

### 稳定版本

- [GitHub Releases](https://github.com/QianMoo0121/Luminara/releases) - 正式版推荐用于生产环境（稳定性优秀），PRE 想要提前尝鲜（稳定性相对正式版较差）

### 开发版本

- [每日构建版本](https://github.com/QianMoo0121/Luminara/actions/workflows/gradle.yml?query=branch%3ATrials) _(需要 GitHub 登录，稳定性相对 PRE 较差)_

### 自行构建

- 克隆本项目到本地 `git clone -b <分支> https://github.com/QianMoo0121/Luminara.git`
- 运行 `./gradlew cleanBuild remapSpigotJar idea --no-daemon -i --stacktrace --refresh-dependencies` 进行配置
- 运行 `./gradlew build collect` 构建项目
- 构建完成后，jar 文件位于 `./build/libs` 目录下

## 🚀 安装使用

1. **下载** jar 文件
2. **启动服务器**：

   ```bash
   java -jar luminara.jar nogui
   ```

   > `nogui` 参数将禁用服务器控制面板

## ❌ 不兼容

- 与所有的优化模组/插件不兼容

> 已包含 Paper 的部分优化

## 📚 支持与帮助

### 📖 文档

- [Arclight 文档](https://wiki.izzel.io/s/arclight-docs) - 详细的使用指南和配置说明
- [待办事项](TODO.md)

### 🐛 问题反馈

- [提交 Bug](https://github.com/QianMoo0121/Luminara/issues/new/choose) - 遇到问题请在这里报告
- [讨论区](https://github.com/QianMoo0121/Luminara/discussions) - 提问和讨论
- 请勿将本服务端的问题反馈到 Arclight！

## 📄 开源协议

本项目基于 [GPL v3](LICENSE) 协议开源。
