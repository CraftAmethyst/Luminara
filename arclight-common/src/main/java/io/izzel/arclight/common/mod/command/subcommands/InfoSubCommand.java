package io.izzel.arclight.common.mod.command.subcommands;

import io.izzel.arclight.i18n.LuminaraVersion;
import org.bukkit.command.CommandSender;

public final class InfoSubCommand implements LuminaraSubCommand {
    @Override
    public String name() {
        return "info";
    }

    @Override
    public String description() {
        return "Show runtime compatibility information";
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage("§6Luminara Version: §f" + LuminaraVersion.version());
        sender.sendMessage("§6Git Commit: §f" + LuminaraVersion.gitCommit());
        sender.sendMessage("§6Minecraft / Loader / Java: §f" + LuminaraVersion.compatibilityLine());
        sender.sendMessage("§6CraftBukkit Package: §f" + LuminaraVersion.bukkitPackage());
        return true;
    }
}
