package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class VersionCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        final String version = Bukkit.getVersion();
        sender.sendMessage("This server is running " + version);
        return true;
    }
}