package io.izzel.arclight.common.mod.command;

import io.izzel.arclight.common.mod.command.subcommands.InfoSubCommand;
import io.izzel.arclight.common.mod.command.subcommands.LuminaraSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public final class LuminaraCommand extends Command {

    private final List<LuminaraSubCommand> subCommands = List.of(new InfoSubCommand());

    public LuminaraCommand() {
        super("luminara", "Luminara runtime commands", "/luminara info", List.of());
        setPermission("luminara.command");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp() && !sender.hasPermission(getPermission())) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        if (args.length == 1) {
            String requested = args[0].toLowerCase(Locale.ROOT);
            for (LuminaraSubCommand subCommand : subCommands) {
                if (subCommand.name().equals(requested)) {
                    return subCommand.execute(sender);
                }
            }
        }
        sender.sendMessage("§6Luminara Commands");
        for (LuminaraSubCommand subCommand : subCommands) {
            sender.sendMessage("§e/luminara " + subCommand.name() + " §7- " + subCommand.description());
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return subCommands.stream().map(LuminaraSubCommand::name).filter(name -> name.startsWith(prefix)).toList();
    }
}
