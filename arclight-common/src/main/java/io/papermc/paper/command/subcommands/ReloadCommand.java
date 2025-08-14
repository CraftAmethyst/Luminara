package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ReloadCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        try {
            Bukkit.getServer().reload();
            sender.sendMessage("Paper config reloaded.");
        } catch (Exception e) {
            sender.sendMessage("Failed to reload Paper config: " + e.getMessage());
        }
        return true;
    }
}