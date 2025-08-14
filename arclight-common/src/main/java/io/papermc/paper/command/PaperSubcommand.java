package io.papermc.paper.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface PaperSubcommand {

    boolean execute(CommandSender sender, String subCommand, String[] args);

    default List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        return Collections.emptyList();
    }

    default boolean tabCompletes() {
        return false;
    }
}