# Arclight

A Bukkit server implementation on common mod loaders.

[![Downloads count](https://img.shields.io/github/downloads/IzzelAliz/Arclight/total?style=flat-square)](https://arclight.izzel.io/)  ![License](https://img.shields.io/github/license/IzzelAliz/Arclight?style=flat-square)

![Logo](.github/arclightlogo.jpg)

## Download

Downloads are available at [https://arclight.izzel.io](https://arclight.izzel.io).

Versions list and support status: [Discussions thread](https://github.com/IzzelAliz/Arclight/discussions/1575)

## Supported platform

| Component           | Supported version                    |
| ------------------- | ------------------------------------ |
| Minecraft           | `1.21.1`                             |
| Fabric              | Loader `0.19.3`                      |
| NeoForge            | `21.1.248`                           |
| CraftBukkit package | `v1_21_R1`                           |
| Java                | `21` (64-bit)                        |
| Luminara            | `1.0.15-beta.1`                      |

On Fabric, Fabric API `0.116.15+1.21.1` and `fabric-permissions-api` `0.3.1` are required.

Minecraft `1.20.1` on Forge is maintained on the [`stable/Trials`](https://github.com/CraftAmethyst/Luminara/tree/stable/Trials) branch. Other Minecraft versions and hybrid loader configurations are not supported.

## Installing

Luminara is distributed as a standard server mod for each supported loader. Install the
matching dedicated server first, then:

1. Put `luminara-neoforge-1.21.1-<version>.jar` in `mods/` on NeoForge, or put
   `luminara-fabric-1.21.1-<version>.jar` in `mods/` on Fabric.
2. Put Bukkit plugins in `plugins/`.
3. Keep Java 21 and start the loader's normal dedicated-server command with `nogui`.
4. On Fabric, also install Fabric API and `fabric-permissions-api` in `mods/`.

The old bootstrap/launcher JARs remain available only as a transitional fallback and
regression reference. They are deprecated and are not the recommended installation path.

Read [the native server regression guide](docs/NATIVE_TESTING.md) for repeatable plugin,
command, scheduler, shutdown, and event checks.


## Support

Read the upstream [Arclight documentation](https://wiki.izzel.io/s/arclight-docs) for behavior inherited from Arclight.  
Something is not working? Report Luminara problems [here](https://github.com/CraftAmethyst/Luminara/issues/new/choose).  

Discord: https://discord.gg/xn8KGphcvS  
QQ group `929252864`: https://qm.qq.com/q/5S00vXfQpq

## License

This project is licensed under [GPL v3](LICENSE).

## Sponsor

[![](.github/bisecthosting.webp)](https://bisecthosting.com/arclight)

Get 25% off hosting server with promocode **arclight** at [BisectHosting](https://bisecthosting.com/arclight).

[![](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com)

YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET
applications. YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.
