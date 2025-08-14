package io.papermc.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class CallbackCommand extends Command {

    protected CallbackCommand(final String name) {
        super(name);
        this.description = "ClickEvent callback";
        this.usageMessage = "/callback <uuid>";
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (args.length != 1) {
            return false;
        }

        final UUID id;
        try {
            id = UUID.fromString(args[0]);
        } catch (final IllegalArgumentException ignored) {
            return false;
        }

        // Note: This would normally use ClickCallbackProviderImpl.CALLBACK_MANAGER.runCallback(sender, id);
        // but since we don't have the full Adventure implementation, we'll just return true
        return true;
    }
}