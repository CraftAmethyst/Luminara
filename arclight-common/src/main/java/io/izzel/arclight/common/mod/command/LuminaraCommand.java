package io.izzel.arclight.common.mod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.mod.command.subcommands.InfoSubCommand;
import io.izzel.arclight.common.mod.command.subcommands.LuminaraSubCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

// Main Luminara command system
public class LuminaraCommand {

    private static final List<LuminaraSubCommand> subCommands = new ArrayList<>();

    static {
        // Register default subcommands
        registerSubCommand(new InfoSubCommand());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("luminara")
                .requires(source -> source.hasPermission(2)) // Lower base permission
                .executes(LuminaraCommand::showHelp)
                .then(Commands.literal("help")
                        .executes(LuminaraCommand::showHelp));

        // Register all subcommands
        for (LuminaraSubCommand subCommand : subCommands) {
            if (subCommand.isEnabled()) {
                command.then(subCommand.build());
            }
        }

        event.getDispatcher().register(command);
    }

    public static void registerSubCommand(LuminaraSubCommand subCommand) {
        subCommands.add(subCommand);
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("§6=== Luminara Commands ==="), false);
        source.sendSuccess(() -> Component.literal("§7Luminara - Enhanced Minecraft server platform"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§e/luminara help §7- Show this help"), false);

        for (LuminaraSubCommand subCommand : subCommands) {
            if (subCommand.isEnabled()) {
                String permissionInfo = subCommand.getRequiredPermissionLevel() > 2 ? " §c[OP]" : "";
                source.sendSuccess(() -> Component.literal("§e/luminara " + subCommand.getName() + permissionInfo + " §7- " + subCommand.getDescription()), false);
            }
        }

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§7Use §e/luminara <command> §7for more details"), false);
        source.sendSuccess(() -> Component.literal("§6========================"), false);
        return 1;
    }

}
