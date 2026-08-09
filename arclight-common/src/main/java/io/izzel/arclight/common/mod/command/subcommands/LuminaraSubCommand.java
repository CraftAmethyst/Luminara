package io.izzel.arclight.common.mod.command.subcommands;

import org.bukkit.command.CommandSender;

public interface LuminaraSubCommand {
    String name();
    String description();
    boolean execute(CommandSender sender);
}
