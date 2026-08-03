package io.izzel.arclight.common.mod.command.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.bridge.core.command.CommandSourceBridge;
import io.izzel.arclight.i18n.LuminaraVersion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

// Server information subcommand
public class InfoSubCommand implements LuminaraSubCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Show Luminara and server information";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .requires(source -> source.hasPermission(getRequiredPermissionLevel()))
                .executes(this::execute);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Lower permission level for info command
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CommandSender sender = ((CommandSourceBridge) source).bridge$getBukkitSender();

        try {
            sender.sendMessage("§6=== Luminara Information ===");
            sender.sendMessage("§eLuminara Version: §f" + LuminaraVersion.version() + " (" + LuminaraVersion.gitCommit() + ")");
            sender.sendMessage("§eMinecraft / Forge / Java: §f" + LuminaraVersion.minecraftVersion() + " / "
                    + LuminaraVersion.forgeVersion() + " / " + LuminaraVersion.javaVersion());
            sender.sendMessage("§eCraftBukkit Package: §f" + LuminaraVersion.bukkitPackage());
            sender.sendMessage("§eBukkit Version: §f" + Bukkit.getBukkitVersion());
            sender.sendMessage("§eOnline Players: §f" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());

            // Runtime information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long usedMemory = totalMemory - freeMemory;

            sender.sendMessage("§eMemory Usage: §f" + usedMemory + "MB / " + maxMemory + "MB");
            sender.sendMessage("§eAvailable Processors: §f" + runtime.availableProcessors());

            sender.sendMessage("§6========================");

            source.sendSuccess(() -> Component.literal("Information displayed"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to show information: " + e.getMessage()));
            return 0;
        }
    }
}
