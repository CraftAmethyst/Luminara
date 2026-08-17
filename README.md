# Arclight

A Bukkit server implementation on common mod loaders.

[![Downloads count](https://img.shields.io/github/downloads/IzzelAliz/Arclight/total?style=flat-square)](https://arclight.izzel.io/)  ![License](https://img.shields.io/github/license/IzzelAliz/Arclight?style=flat-square)

![Logo](.github/arclightlogo.jpg)

## Download

Downloads are available at [https://arclight.izzel.io](https://arclight.izzel.io).

Versions list and support status: [Discussions thread](https://github.com/IzzelAliz/Arclight/discussions/1575)

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

Read the [document](https://wiki.izzel.io/s/arclight-docs).  
Something is not working? Report any problems [here](https://github.com/IzzelAliz/Arclight/issues/new/choose)!.  
Questions and discussions [here](https://github.com/IzzelAliz/Arclight/discussions).  

Discord Server: https://discord.gg/ZvTY5SC  
QQ Group Chat: 3556966

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
