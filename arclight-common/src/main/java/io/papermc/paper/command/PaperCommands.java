package io.papermc.paper.command;

import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class PaperCommands {

    private PaperCommands() {
    }

    private static final Map<String, Command> COMMANDS = new HashMap<>();
    static {
        COMMANDS.put("paper", new PaperCommand("paper"));
        COMMANDS.put("callback", new CallbackCommand("callback"));
    }

    public static void registerCommands(final MinecraftServer server) {
        COMMANDS.forEach((name, command) -> {
            server.getCommands().getDispatcher().register(
                net.minecraft.commands.Commands.literal(name)
                    .executes(context -> {
                        // This would normally integrate with the command system
                        // For now, we'll just return success
                        return 1;
                    })
            );
        });
    }

    public static Map<String, Command> getCommands() {
        return COMMANDS;
    }
}