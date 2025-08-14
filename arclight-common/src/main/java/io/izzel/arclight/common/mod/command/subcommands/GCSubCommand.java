package io.izzel.arclight.common.mod.command.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

// Garbage Collection subcommand

public class GCSubCommand implements LuminaraSubCommand {

    @Override
    public String getName() {
        return "gc";
    }

    @Override
    public String getDescription() {
        return "Force cache cleanup and garbage collection";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .requires(source -> source.hasPermission(getRequiredPermissionLevel()))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            // Simple memory usage calculation without MPEM
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double beforeUsage = (double) (totalMemory - freeMemory) / runtime.maxMemory();

            // Perform basic cleanup
            System.runFinalization();
            System.gc();

            Thread.sleep(1000);

            totalMemory = runtime.totalMemory();
            freeMemory = runtime.freeMemory();
            double afterUsage = (double) (totalMemory - freeMemory) / runtime.maxMemory();
            double freed = (beforeUsage - afterUsage) * 100;

            source.sendSuccess(() -> Component.literal(String.format("Basic GC completed. Freed: %.2f%%", freed)), true);
            source.sendSuccess(() -> Component.literal(String.format("Memory usage: %.2f%% -> %.2f%%", beforeUsage * 100, afterUsage * 100)), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to perform cleanup: " + e.getMessage()));
            return 0;
        }
    }
}
